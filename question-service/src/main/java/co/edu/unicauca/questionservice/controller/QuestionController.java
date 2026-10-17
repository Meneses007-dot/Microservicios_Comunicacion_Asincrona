package co.edu.unicauca.questionservice.controller;

import co.edu.unicauca.questionservice.dto.CreateQuestionRequest;
import co.edu.unicauca.questionservice.dto.CreateQuestionResponse;
import co.edu.unicauca.questionservice.dto.QuestionResponse;
import co.edu.unicauca.questionservice.dto.SubmitResponse;
import co.edu.unicauca.questionservice.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    /**
     * POST /questions → 201 {id, status:"DRAFT", message} si valida;
     * 400 con errores si no valida; 503 si Validation Service no responde.
     * El encabezado Authorization es opcional y no se valida (no hay JWT aún).
     */
    @PostMapping
    public ResponseEntity<CreateQuestionResponse> create(
            @Valid @RequestBody CreateQuestionRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        QuestionResponse created = questionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreateQuestionResponse(created.id(), created.status(),
                        "Pregunta creada y validada correctamente"));
    }

    /** GET /questions/{id} → pregunta, o 404. */
    @GetMapping("/{id}")
    public QuestionResponse get(@PathVariable Long id) {
        return questionService.get(id);
    }

    /** PUT /questions/{id} → corrige (revalida) y devuelve la pregunta actualizada. */
    @PutMapping("/{id}")
    public QuestionResponse update(@PathVariable Long id,
                                   @Valid @RequestBody CreateQuestionRequest request) {
        return questionService.update(id, request);
    }

    /**
     * POST /questions/{questionId}/submit → 202 {message, operationId, status:"PROCESSING"}.
     * No espera a Lifecycle; el resultado se consulta luego.
     */
    @PostMapping("/{questionId}/submit")
    public ResponseEntity<SubmitResponse> submit(
            @PathVariable Long questionId,
            @RequestHeader(value = "Authorization", required = false) String requestedBy) {
        SubmitResponse response = questionService.submit(questionId, requestedBy);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
