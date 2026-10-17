package co.edu.unicauca.questionservice.controller;

import co.edu.unicauca.questionservice.dto.OperationResponse;
import co.edu.unicauca.questionservice.service.QuestionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Consulta del resultado de las operaciones asíncronas (decisión de diseño nº 9).
 */
@RestController
public class OperationController {

    private final QuestionService questionService;

    public OperationController(QuestionService questionService) {
        this.questionService = questionService;
    }

    /**
     * GET /operations/{operationId} → {operationId, questionId, type, status, message}.
     * status = PENDING | COMPLETED | FAILED. 503 si Lifecycle no responde.
     */
    @GetMapping("/operations/{operationId}")
    public OperationResponse getOperation(@PathVariable String operationId) {
        return questionService.getOperation(operationId);
    }
}
