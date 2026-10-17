package co.edu.unicauca.lifecycleservice.service;

import co.edu.unicauca.lifecycleservice.dto.LifecycleStateResponse;
import co.edu.unicauca.lifecycleservice.dto.OperationResponse;
import co.edu.unicauca.lifecycleservice.dto.TransitionResponse;
import co.edu.unicauca.lifecycleservice.exception.InvalidTransitionException;
import co.edu.unicauca.lifecycleservice.exception.ResourceNotFoundException;
import co.edu.unicauca.lifecycleservice.messaging.SubmitCommandDTO;
import co.edu.unicauca.lifecycleservice.model.LifecycleRecord;
import co.edu.unicauca.lifecycleservice.model.OperationResult;
import co.edu.unicauca.lifecycleservice.model.OperationStatus;
import co.edu.unicauca.lifecycleservice.model.QuestionStatus;
import co.edu.unicauca.lifecycleservice.repository.LifecycleRecordRepository;
import co.edu.unicauca.lifecycleservice.repository.OperationResultRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Máquina de estados del ciclo de vida. Es la <b>única</b> clase que modifica
 * el estado de una pregunta.
 */
@Service
public class LifecycleService {

    private static final Logger log = LoggerFactory.getLogger(LifecycleService.class);

    private static final String OPERATION_TYPE_SUBMIT = "SUBMIT_FOR_REVIEW";

    /** Transiciones permitidas (decisión de diseño nº 5 y sección 7 del instructivo). */
    private static final Map<QuestionStatus, Set<QuestionStatus>> ALLOWED = new EnumMap<>(QuestionStatus.class);

    static {
        ALLOWED.put(QuestionStatus.DRAFT, EnumSet.of(QuestionStatus.PENDING_REVIEW));
        ALLOWED.put(QuestionStatus.PENDING_REVIEW, EnumSet.of(QuestionStatus.IN_REVIEW));
        ALLOWED.put(QuestionStatus.IN_REVIEW, EnumSet.of(QuestionStatus.APPROVED, QuestionStatus.REJECTED));
        ALLOWED.put(QuestionStatus.REJECTED, EnumSet.of(QuestionStatus.DRAFT));
        ALLOWED.put(QuestionStatus.APPROVED, EnumSet.noneOf(QuestionStatus.class));
    }

    private final LifecycleRecordRepository recordRepository;
    private final OperationResultRepository operationRepository;

    public LifecycleService(LifecycleRecordRepository recordRepository,
                            OperationResultRepository operationRepository) {
        this.recordRepository = recordRepository;
        this.operationRepository = operationRepository;
    }

    @Transactional(readOnly = true)
    public LifecycleStateResponse getState(Long questionId) {
        LifecycleRecord record = recordRepository.findByQuestionId(questionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No hay registro de ciclo de vida para la pregunta " + questionId));
        return new LifecycleStateResponse(record.getQuestionId(), record.getState(), record.getChangedAt(),
                record.getChangedBy(), record.getComment());
    }

    /**
     * Aplica una transición validando la máquina de estados.
     * Si no hay registro previo, el estado implícito es DRAFT.
     *
     * @throws InvalidTransitionException si la transición no está permitida (409)
     */
    @Transactional
    public TransitionResponse transition(Long questionId, QuestionStatus target, String requestedBy, String comment) {
        LifecycleRecord record = recordRepository.findByQuestionId(questionId).orElse(null);
        QuestionStatus previous = (record == null) ? QuestionStatus.DRAFT : record.getState();

        if (!isAllowed(previous, target)) {
            throw new InvalidTransitionException(previous, target);
        }

        apply(record, questionId, target, requestedBy, comment);
        log.info("Transición aplicada: pregunta {} {} -> {}", questionId, previous, target);
        return new TransitionResponse(questionId, previous, target);
    }

    /** Convierte un texto a QuestionStatus validando el valor (400 si es inválido). */
    public QuestionStatus parseStatus(String targetState) {
        try {
            return QuestionStatus.valueOf(targetState.trim().toUpperCase());
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Estado desconocido: '" + targetState + "'");
        }
    }

    /**
     * Procesa el comando asincrónico de envío a revisión.
     * <ul>
     *   <li>es idempotente: si ya existe un OperationResult con ese operationId, se ignora;</li>
     *   <li>si la transición DRAFT → PENDING_REVIEW es válida, guarda COMPLETED;</li>
     *   <li>si no, guarda FAILED con el motivo y <b>no</b> cambia el estado.</li>
     * </ul>
     */
    @Transactional
    public void processSubmitCommand(SubmitCommandDTO command) {
        if (command == null || command.operationId() == null || command.questionId() == null) {
            log.warn("Comando de envío incompleto; se ignora: {}", command);
            return;
        }
        if (operationRepository.existsById(command.operationId())) {
            log.info("Comando duplicado ignorado (idempotencia). operationId={}", command.operationId());
            return;
        }

        OperationResult result = new OperationResult(command.operationId(), command.questionId(),
                OPERATION_TYPE_SUBMIT, OperationStatus.PENDING, "Procesando envío a revisión", Instant.now());
        operationRepository.save(result);

        try {
            transition(command.questionId(), QuestionStatus.PENDING_REVIEW, command.requestedBy(),
                    "Envío a revisión solicitado por Question Service");
            result.markCompleted("La pregunta pasó a PENDING_REVIEW");
            log.info("Comando procesado. questionId={} operationId={}", command.questionId(), command.operationId());
        } catch (InvalidTransitionException ex) {
            result.markFailed(ex.getMessage());
            log.warn("Comando rechazado. questionId={} operationId={} motivo={}",
                    command.questionId(), command.operationId(), ex.getMessage());
        }

        operationRepository.save(result);
    }

    @Transactional(readOnly = true)
    public OperationResponse getOperation(String operationId) {
        OperationResult result = operationRepository.findById(operationId)
                .orElseThrow(() -> new ResourceNotFoundException("Operación " + operationId + " no encontrada"));
        return new OperationResponse(result.getOperationId(), result.getQuestionId(), result.getType(),
                result.getStatus(), result.getMessage(), result.getCreatedAt(), result.getCompletedAt());
    }

    private static boolean isAllowed(QuestionStatus from, QuestionStatus to) {
        return ALLOWED.getOrDefault(from, Set.of()).contains(to);
    }

    private void apply(LifecycleRecord record, Long questionId, QuestionStatus target,
                       String requestedBy, String comment) {
        LifecycleRecord entity = (record != null) ? record : new LifecycleRecord();
        entity.setQuestionId(questionId);
        entity.setState(target);
        entity.setChangedAt(Instant.now());
        entity.setChangedBy(requestedBy);
        entity.setComment(comment);
        recordRepository.save(entity);
    }
}
