package co.edu.unicauca.lifecycleservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Estado actual de una pregunta. Un solo registro por pregunta.
 * Si no existe registro, el estado implícito es DRAFT (ver LifecycleService).
 */
@Entity
@Table(name = "lifecycle_records")
public class LifecycleRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long questionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private QuestionStatus state;

    @Column(nullable = false)
    private Instant changedAt;

    private String changedBy;

    @Column(length = 500)
    private String comment;

    public LifecycleRecord() {
    }

    public LifecycleRecord(Long questionId, QuestionStatus state, Instant changedAt, String changedBy, String comment) {
        this.questionId = questionId;
        this.state = state;
        this.changedAt = changedAt;
        this.changedBy = changedBy;
        this.comment = comment;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public QuestionStatus getState() {
        return state;
    }

    public void setState(QuestionStatus state) {
        this.state = state;
    }

    public Instant getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(Instant changedAt) {
        this.changedAt = changedAt;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
