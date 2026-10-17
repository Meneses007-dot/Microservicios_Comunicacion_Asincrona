package co.edu.unicauca.questionservice.service;

import co.edu.unicauca.questionservice.client.LifecycleClient;
import co.edu.unicauca.questionservice.client.ValidationClient;
import co.edu.unicauca.questionservice.dto.CreateQuestionRequest;
import co.edu.unicauca.questionservice.dto.LifecycleOperationDTO;
import co.edu.unicauca.questionservice.dto.OptionDTO;
import co.edu.unicauca.questionservice.dto.OperationResponse;
import co.edu.unicauca.questionservice.dto.QuestionResponse;
import co.edu.unicauca.questionservice.dto.SubmitResponse;
import co.edu.unicauca.questionservice.dto.ValidationRequest;
import co.edu.unicauca.questionservice.dto.ValidationResult;
import co.edu.unicauca.questionservice.exception.InvalidQuestionException;
import co.edu.unicauca.questionservice.exception.ResourceNotFoundException;
import co.edu.unicauca.questionservice.exception.ServiceUnavailableException;
import co.edu.unicauca.questionservice.messaging.CommandPublisher;
import co.edu.unicauca.questionservice.messaging.SubmitCommandDTO;
import co.edu.unicauca.questionservice.model.Question;
import co.edu.unicauca.questionservice.model.QuestionOption;
import co.edu.unicauca.questionservice.repository.QuestionRepository;
import org.springframework.amqp.AmqpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class QuestionService {

    private static final Logger log = LoggerFactory.getLogger(QuestionService.class);
    private static final String OPERATION_TYPE_SUBMIT = "SUBMIT_FOR_REVIEW";
    private static final String STATE_REJECTED = "REJECTED";

    private final QuestionRepository questionRepository;
    private final ValidationClient validationClient;
    private final LifecycleClient lifecycleClient;
    private final CommandPublisher commandPublisher;

    public QuestionService(QuestionRepository questionRepository,
                           ValidationClient validationClient,
                           LifecycleClient lifecycleClient,
                           CommandPublisher commandPublisher) {
        this.questionRepository = questionRepository;
        this.validationClient = validationClient;
        this.lifecycleClient = lifecycleClient;
        this.commandPublisher = commandPublisher;
    }

    /**
     * Paso 2-3 del flujo: valida de forma síncrona y, si es válida, persiste en DRAFT (201).
     * Si la validación falla, lanza InvalidQuestionException (400) y no persiste nada.
     */
    @Transactional
    public QuestionResponse create(CreateQuestionRequest request) {
        ValidationResult validation = validationClient.validate(toValidationRequest(request));
        if (!validation.valid()) {
            log.info("Pregunta inválida; no se persiste. Errores: {}", validation.errors());
            throw new InvalidQuestionException("La pregunta no pasó la validación", validation.errors());
        }

        Question question = new Question();
        apply(request, question);
        question.setStatus("DRAFT");
        question.setCreatedAt(Instant.now());
        question.setUpdatedAt(Instant.now());

        Question saved = questionRepository.save(question);
        log.info("Pregunta creada id={} en estado informativo DRAFT", saved.getId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public QuestionResponse get(Long id) {
        return toResponse(findOrThrow(id));
    }

    /**
     * Corrige una pregunta. Vuelve a validar antes de guardar y, si Lifecycle
     * reporta REJECTED, solicita la transición REJECTED → DRAFT para reingresar
     * al proceso de revisión por el flujo normal.
     */
    @Transactional
    public QuestionResponse update(Long id, CreateQuestionRequest request) {
        Question question = findOrThrow(id);

        ValidationResult validation = validationClient.validate(toValidationRequest(request));
        if (!validation.valid()) {
            throw new InvalidQuestionException("La pregunta corregida no pasó la validación", validation.errors());
        }

        lifecycleClient.getState(id).ifPresent(state -> {
            if (STATE_REJECTED.equalsIgnoreCase(state.state())) {
                lifecycleClient.transition(id, "DRAFT", "question-service",
                        "Pregunta corregida; vuelve a borrador para reenviarse a revisión");
            }
        });

        apply(request, question);
        question.setUpdatedAt(Instant.now());
        return toResponse(questionRepository.save(question));
    }

    /**
     * Paso 4-5: publica el comando de envío y responde 202 de inmediato.
     * No verifica el estado (lo valida Lifecycle de forma asíncrona).
     */
    public SubmitResponse submit(Long questionId, String requestedBy) {
        Question question = findOrThrow(questionId);

        String operationId = UUID.randomUUID().toString();
        String by = (requestedBy != null && !requestedBy.isBlank())
                ? requestedBy
                : "author:" + question.getAuthorId();

        try {
            commandPublisher.publishSubmit(new SubmitCommandDTO(questionId, operationId, by, Instant.now()));
        } catch (AmqpException ex) {
            log.error("No fue posible publicar el comando de envío: {}", ex.getMessage());
            throw new ServiceUnavailableException(
                    "El broker de mensajería no está disponible. Intente más tarde.", ex);
        }
        return new SubmitResponse(
                "Solicitud aceptada para procesamiento. Consulte el resultado con el operationId.",
                operationId, "PROCESSING");
    }

    /**
     * Delega en Lifecycle. Si aún no hay registro de la operación, devuelve PENDING.
     */
    @Transactional(readOnly = true)
    public OperationResponse getOperation(String operationId) {
        Optional<LifecycleOperationDTO> operation = lifecycleClient.getOperation(operationId);
        if (operation.isPresent()) {
            LifecycleOperationDTO dto = operation.get();
            return new OperationResponse(dto.operationId(), dto.questionId(), dto.type(),
                    dto.status(), dto.message());
        }
        return new OperationResponse(operationId, null, OPERATION_TYPE_SUBMIT, "PENDING",
                "La operación aún no ha sido procesada por el servicio de ciclo de vida");
    }

    private Question findOrThrow(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("La pregunta " + id + " no existe"));
    }

    private static ValidationRequest toValidationRequest(CreateQuestionRequest request) {
        return new ValidationRequest(request.text(), request.options(), request.correctOptionId());
    }

    private static void apply(CreateQuestionRequest request, Question question) {
        question.setTitle(request.title());
        question.setText(request.text());
        question.setCorrectOptionId(request.correctOptionId());
        if (request.authorId() != null) {
            question.setAuthorId(request.authorId());
        }
        List<QuestionOption> options = new ArrayList<>();
        if (request.options() != null) {
            for (OptionDTO option : request.options()) {
                if (option != null) {
                    options.add(new QuestionOption(option.id(), option.text()));
                }
            }
        }
        question.setOptions(options);
    }

    private static QuestionResponse toResponse(Question question) {
        List<OptionDTO> options = question.getOptions().stream()
                .map(o -> new OptionDTO(o.getOptionId(), o.getText()))
                .toList();
        return new QuestionResponse(question.getId(), question.getTitle(), question.getText(),
                question.getAuthorId(), question.getStatus(), options, question.getCorrectOptionId(),
                question.getCreatedAt(), question.getUpdatedAt());
    }
}
