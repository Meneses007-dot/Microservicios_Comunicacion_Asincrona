package co.edu.unicauca.notificationservice.controller;

import co.edu.unicauca.notificationservice.model.NotificationRecord;
import co.edu.unicauca.notificationservice.service.EmailService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoint de solo lectura recomendado para mostrar la evidencia en Postman:
 * las notificaciones generadas de forma asíncrona.
 */
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final EmailService emailService;

    public NotificationController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping
    public List<NotificationRecord> findAll() {
        return emailService.findAll();
    }
}
