package co.edu.unicauca.reviewservice.service;

import co.edu.unicauca.reviewservice.client.LifecycleClient;
import co.edu.unicauca.reviewservice.client.UserClient;
import co.edu.unicauca.reviewservice.dto.AssignRequest;
import co.edu.unicauca.reviewservice.dto.AssignResponse;
import co.edu.unicauca.reviewservice.dto.LifecycleStateDTO;
import co.edu.unicauca.reviewservice.dto.ResultRequest;
import co.edu.unicauca.reviewservice.dto.ResultResponse;
import co.edu.unicauca.reviewservice.dto.UserDTO;
import co.edu.unicauca.reviewservice.exception.ConflictException;
import co.edu.unicauca.reviewservice.exception.UnprocessableEntityException;
import co.edu.unicauca.reviewservice.messaging.EventPublisher;
import co.edu.unicauca.reviewservice.messaging.ReviewerAssigned;
import co.edu.unicauca.reviewservice.model.AssignmentStatus;
import co.edu.unicauca.reviewservice.model.ReviewAssignment;
import co.edu.unicauca.reviewservice.model.ReviewRecord;
import co.edu.unicauca.reviewservice.repository.ReviewAssignmentRepository;
import co.edu.unicauca.reviewservice.repository.ReviewRecordRepository;
import org.springframework.amqp.AmqpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Orquesta la asignación y el resultado de revisiones.
 *
 * <p>La asignación se guarda primero en PENDING y el estado de la pregunta solo
 * se cambia a través de Lifecycle. Si Lifecycle falla, la asignación se marca
 * FAILED y no se publica ningún evento, de modo que asignación y estado nunca
 * quedan inconsistentes sin rastro.</p>
 *
 * <p>Nota: los métodos no son {@code @Transactional} a propósito; cada
 * {@code save} confirma por separado y así el registro FAILED sobrevive aunque
 * se lance la excepción.</p>
 */
@Service
public class ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewService.class);

    private static final String STATE_PENDING_REVIEW = "PENDING_REVIEW";
    private static final String STATE_IN_REVIEW = "IN_REVIEW";
    private static final String ROLE_REVIEWER = "REVIEWER";

    private final ReviewAssignmentRepository assignmentRepository;
    private final ReviewRecordRepository reviewRecordRepository;
    private final LifecycleClient lifecycleClient;
    private final UserClient userClient;
    private final EventPublisher eventPublisher;

    public ReviewService(ReviewAssignmentRepository assignmentRepository,
                         ReviewRecordRepository reviewRecordRepository,
                         LifecycleClient lifecycleClient,
                         UserClient userClient,
                         EventPublisher eventPublisher) {
        this.assignmentRepository = assignmentRepository;
        this.reviewRecordRepository = reviewRecordRepository;
        this.lifecycleClient = lifecycleClient;
        this.userClient = userClient;
        this.eventPublisher = eventPublisher;
    }

    /** Paso 7 del flujo: asignación de revisor con manejo consistente de fallos. */
    public AssignResponse assign(AssignRequest request) {
        // (a) la pregunta debe estar en PENDING_REVIEW
        String current = lifecycleClient.getState(request.questionId())
                .map(LifecycleStateDTO::state)
                .orElse("DRAFT");
        if (!STATE_PENDING_REVIEW.equalsIgnoreCase(current)) {
            throw new ConflictException("La pregunta " + request.questionId()
                    + " no está en PENDING_REVIEW (estado actual: " + current + ")");
        }

        // (b) el revisor debe existir y tener rol REVIEWER
        UserDTO reviewer = userClient.getUser(request.reviewerId());
        if (reviewer == null || reviewer.roles() == null || !reviewer.roles().contains(ROLE_REVIEWER)) {
            throw new UnprocessableEntityException(
                    "El usuario " + request.reviewerId() + " no tiene el rol REVIEWER");
        }

        // (c) guardar la asignación en PENDING
        ReviewAssignment assignment = assignmentRepository.save(
                new ReviewAssignment(request.questionId(), request.reviewerId(), request.assignedBy()));

        // (d) solicitar la transición PENDING_REVIEW -> IN_REVIEW
        try {
            lifecycleClient.transition(request.questionId(), STATE_IN_REVIEW,
                    "review-service:" + request.assignedBy(),
                    "Revisor " + request.reviewerId() + " asignado a la pregunta");
        } catch (RuntimeException ex) {
            assignment.setStatus(AssignmentStatus.FAILED);
            assignmentRepository.save(assignment);
            log.warn("Asignación FAILED (questionId={}, reviewerId={}): {}",
                    request.questionId(), request.reviewerId(), ex.getMessage());
            throw ex;
        }

        // (e) confirmar la asignación
        assignment.setStatus(AssignmentStatus.ASSIGNED);
        assignmentRepository.save(assignment);

        // (f) solo ahora publicar el evento asíncrono. Si el broker no está disponible
        // la asignación NO se cae: la notificación es asíncrona y no debe bloquear el flujo.
        try {
            eventPublisher.publishReviewerAssigned(new ReviewerAssigned(
                    request.questionId(), request.reviewerId(), request.assignedBy(), Instant.now()));
        } catch (AmqpException ex) {
            log.warn("No se pudo publicar ReviewerAssigned (questionId={}): {}",
                    request.questionId(), ex.getMessage());
        }

        return new AssignResponse(assignment.getId(), request.questionId(),
                request.reviewerId(), assignment.getStatus().name());
    }

    /** Registra el resultado de la revisión y solicita la transición IN_REVIEW → APPROVED/REJECTED. */
    public ResultResponse registerResult(Long questionId, ResultRequest request) {
        String result = (request.result() == null) ? "" : request.result().trim().toUpperCase();
        if (!"APPROVED".equals(result) && !"REJECTED".equals(result)) {
            throw new IllegalArgumentException("result debe ser APPROVED o REJECTED");
        }
        if ("REJECTED".equals(result) && (request.comments() == null || request.comments().isBlank())) {
            throw new IllegalArgumentException("El motivo (comments) es obligatorio al rechazar");
        }

        String current = lifecycleClient.getState(questionId)
                .map(LifecycleStateDTO::state)
                .orElse("DRAFT");
        if (!STATE_IN_REVIEW.equalsIgnoreCase(current)) {
            throw new ConflictException("La pregunta " + questionId
                    + " no está en IN_REVIEW (estado actual: " + current + ")");
        }

        ReviewRecord record = reviewRecordRepository.save(
                new ReviewRecord(questionId, request.reviewerId(), result, request.comments()));

        try {
            lifecycleClient.transition(questionId, result, "review-service:" + request.reviewerId(),
                    request.comments());
            record.setStatus("APPLIED");
        } catch (RuntimeException ex) {
            record.setStatus("FAILED");
            reviewRecordRepository.save(record);
            log.warn("Revisión FAILED (questionId={}): {}", questionId, ex.getMessage());
            throw ex;
        }

        reviewRecordRepository.save(record);
        log.info("Revisión registrada questionId={} result={} status={}", questionId, result, record.getStatus());
        return new ResultResponse(record.getId(), questionId, result, record.getStatus());
    }
}
