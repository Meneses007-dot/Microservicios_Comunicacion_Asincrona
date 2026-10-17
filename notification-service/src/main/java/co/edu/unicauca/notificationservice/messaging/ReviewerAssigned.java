package co.edu.unicauca.notificationservice.messaging;

import java.time.Instant;

/**
 * Evento ReviewerAssigned publicado por Review Service.
 * Mismo JSON; la clase se duplica por servicio.
 */
public record ReviewerAssigned(
        Long questionId,
        Long reviewerId,
        String assignedBy,
        Instant occurredAt) {
}
