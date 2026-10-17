package co.edu.unicauca.lifecycleservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Resultado de una operación asíncrona (por ejemplo, el envío a revisión).
 * El {@code operationId} (UUID generado por Question Service) es la clave y,
 * además, el mecanismo de idempotencia del listener.
 */
@Entity
@Table(name = "operation_results")
public class OperationResult {

    @Id
    @Column(length = 64)
    private String operationId;

    @Column(nullable = false)
    private Long questionId;

    /** Tipo lógico de la operación, por ejemplo SUBMIT_FOR_REVIEW. */
    @Column(nullable = false, length = 50)
    private String type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OperationStatus status;

    @Column(length = 500)
    private String message;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant completedAt;

    public OperationResult() {
    }

    public OperationResult(String operationId, Long questionId, String type, OperationStatus status,
                           String message, Instant createdAt) {
        this.operationId = operationId;
        this.questionId = questionId;
        this.type = type;
        this.status = status;
        this.message = message;
        this.createdAt = createdAt;
    }

    public void markCompleted(String message) {
        this.status = OperationStatus.COMPLETED;
        this.message = message;
        this.completedAt = Instant.now();
    }

    public void markFailed(String message) {
        this.status = OperationStatus.FAILED;
        this.message = message;
        this.completedAt = Instant.now();
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public OperationStatus getStatus() {
        return status;
    }

    public void setStatus(OperationStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}
