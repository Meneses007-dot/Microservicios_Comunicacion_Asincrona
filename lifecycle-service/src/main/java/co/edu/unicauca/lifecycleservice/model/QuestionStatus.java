package co.edu.unicauca.lifecycleservice.model;

/**
 * Estados del ciclo de vida de una pregunta.
 * Enum base de la guía (DRAFT, PENDING_REVIEW, IN_REVIEW, APPROVED) más
 * REJECTED, exigido por la matriz de pruebas (decisión de diseño nº 5).
 * No se implementan PUBLISHED ni ARCHIVED.
 */
public enum QuestionStatus {
    DRAFT,
    PENDING_REVIEW,
    IN_REVIEW,
    APPROVED,
    REJECTED
}
