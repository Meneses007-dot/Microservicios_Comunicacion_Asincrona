package co.edu.unicauca.lifecycleservice.config;

/**
 * Constantes de mensajería del servicio.
 * Se duplican por servicio (no se comparte una librería de DTO/config) a propósito,
 * para que cada microservicio sea desplegable de forma independiente.
 */
public final class MessagingConstants {

    /** Exchange directo y durable compartido por todo el ecosistema. */
    public static final String EXCHANGE = "saberpro.direct.exchange";

    /** Cola y routing key del comando de envío a revisión (Question → Lifecycle). */
    public static final String SUBMIT_QUEUE = "question.submit.review.queue";
    public static final String SUBMIT_ROUTING_KEY = "question.submit";

    /** Cola y routing key del evento de asignación de revisor (Review → Notification). */
    public static final String REVIEWER_ASSIGNED_QUEUE = "notification.reviewer.assigned.queue";
    public static final String REVIEWER_ASSIGNED_ROUTING_KEY = "reviewer.assigned";

    private MessagingConstants() {
    }
}
