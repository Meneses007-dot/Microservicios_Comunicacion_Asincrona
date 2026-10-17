package co.edu.unicauca.reviewservice.config;

/** Constantes de mensajería (duplicadas por servicio de forma intencional). */
public final class MessagingConstants {

    public static final String EXCHANGE = "saberpro.direct.exchange";
    public static final String SUBMIT_QUEUE = "question.submit.review.queue";
    public static final String SUBMIT_ROUTING_KEY = "question.submit";
    public static final String REVIEWER_ASSIGNED_QUEUE = "notification.reviewer.assigned.queue";
    public static final String REVIEWER_ASSIGNED_ROUTING_KEY = "reviewer.assigned";

    private MessagingConstants() {
    }
}
