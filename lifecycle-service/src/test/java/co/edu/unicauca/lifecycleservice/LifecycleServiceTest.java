package co.edu.unicauca.lifecycleservice;

import co.edu.unicauca.lifecycleservice.dto.TransitionResponse;
import co.edu.unicauca.lifecycleservice.exception.InvalidTransitionException;
import co.edu.unicauca.lifecycleservice.messaging.SubmitCommandDTO;
import co.edu.unicauca.lifecycleservice.model.OperationStatus;
import co.edu.unicauca.lifecycleservice.model.QuestionStatus;
import co.edu.unicauca.lifecycleservice.repository.LifecycleRecordRepository;
import co.edu.unicauca.lifecycleservice.repository.OperationResultRepository;
import co.edu.unicauca.lifecycleservice.service.LifecycleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pruebas de la máquina de estados y del procesamiento del comando asíncrono.
 * Se usa {@code @DataJpaTest} para no depender de un broker RabbitMQ en la
 * ejecución de las pruebas: el listener solo delega en {@link LifecycleService}.
 */
@DataJpaTest
class LifecycleServiceTest {

    @Autowired
    private LifecycleRecordRepository recordRepository;

    @Autowired
    private OperationResultRepository operationRepository;

    private LifecycleService service() {
        return new LifecycleService(recordRepository, operationRepository);
    }

    @Test
    void transicionValidaDeDraftAPendingReview() {
        TransitionResponse response = service().transition(1L, QuestionStatus.PENDING_REVIEW, "teacher", "envío");
        assertThat(response.previousState()).isEqualTo(QuestionStatus.DRAFT);
        assertThat(response.currentState()).isEqualTo(QuestionStatus.PENDING_REVIEW);
    }

    @Test
    void transicionInvalidaDeDraftAApprovedEsRechazada() {
        assertThrows(InvalidTransitionException.class,
                () -> service().transition(1L, QuestionStatus.APPROVED, "teacher", null));
    }

    @Test
    void transicionDeRejectedADraftPermitida() {
        LifecycleService service = service();
        service.transition(1L, QuestionStatus.PENDING_REVIEW, "teacher", null);
        service.transition(1L, QuestionStatus.IN_REVIEW, "review", null);
        service.transition(1L, QuestionStatus.REJECTED, "review", "motivo");

        TransitionResponse response = service.transition(1L, QuestionStatus.DRAFT, "question-service", "corrección");
        assertThat(response.previousState()).isEqualTo(QuestionStatus.REJECTED);
        assertThat(response.currentState()).isEqualTo(QuestionStatus.DRAFT);
    }

    @Test
    void comandoExitosoRegistraCompletedYPendingReview() {
        LifecycleService service = service();
        service.processSubmitCommand(new SubmitCommandDTO(1L, "op-1", "teacher", Instant.now()));

        assertThat(operationRepository.findById("op-1")).get()
                .extracting(r -> r.getStatus()).isEqualTo(OperationStatus.COMPLETED);
        assertThat(recordRepository.findByQuestionId(1L)).get()
                .extracting(r -> r.getState()).isEqualTo(QuestionStatus.PENDING_REVIEW);
    }

    @Test
    void comandoSobreEstadoNoDraftRegistraFailedYSinCambiarEstado() {
        LifecycleService service = service();
        service.transition(1L, QuestionStatus.PENDING_REVIEW, "teacher", null);

        // Un segundo envío (ya no está en DRAFT) debe fallar y no cambiar el estado
        service.processSubmitCommand(new SubmitCommandDTO(1L, "op-2", "teacher", Instant.now()));

        assertThat(operationRepository.findById("op-2")).get()
                .extracting(r -> r.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(recordRepository.findByQuestionId(1L)).get()
                .extracting(r -> r.getState()).isEqualTo(QuestionStatus.PENDING_REVIEW);
    }

    @Test
    void comandoEsIdempotenteConElMismoOperationId() {
        LifecycleService service = service();
        SubmitCommandDTO command = new SubmitCommandDTO(1L, "op-3", "teacher", Instant.now());
        service.processSubmitCommand(command);
        service.processSubmitCommand(command);

        assertThat(operationRepository.findAll()).hasSize(1);
    }
}
