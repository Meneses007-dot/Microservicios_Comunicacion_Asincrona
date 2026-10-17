package co.edu.unicauca.reviewservice.messaging;

import java.time.Instant;

/**
 * Evento ReviewerAssigned: lo publica Review Service y lo consume Notification
 * Service. Mismo JSON que la clase homónima del consumidor.
 */
public record ReviewerAssigned(
        Long questionId,
        Long reviewerId,
        String assignedBy,
        Instant occurredAt) {
}
