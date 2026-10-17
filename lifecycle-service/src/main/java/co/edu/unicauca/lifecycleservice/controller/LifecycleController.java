package co.edu.unicauca.lifecycleservice.controller;

import co.edu.unicauca.lifecycleservice.dto.LifecycleStateResponse;
import co.edu.unicauca.lifecycleservice.dto.OperationResponse;
import co.edu.unicauca.lifecycleservice.dto.TransitionRequest;
import co.edu.unicauca.lifecycleservice.dto.TransitionResponse;
import co.edu.unicauca.lifecycleservice.model.QuestionStatus;
import co.edu.unicauca.lifecycleservice.service.LifecycleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lifecycle")
public class LifecycleController {

    private final LifecycleService lifecycleService;

    public LifecycleController(LifecycleService lifecycleService) {
        this.lifecycleService = lifecycleService;
    }

    /** GET /lifecycle/{questionId} → estado actual, o 404 si no hay registro. */
    @GetMapping("/{questionId}")
    public LifecycleStateResponse getState(@PathVariable Long questionId) {
        return lifecycleService.getState(questionId);
    }

    /**
     * POST /lifecycle/transition → 200 con estado anterior y actual,
     * o 409 si la transición no está permitida.
     */
    @PostMapping("/transition")
    public TransitionResponse transition(@Valid @RequestBody TransitionRequest request) {
        QuestionStatus target = lifecycleService.parseStatus(request.targetState());
        return lifecycleService.transition(request.questionId(), target, request.requestedBy(), request.comment());
    }

    /** GET /lifecycle/operations/{operationId} → resultado de una operación asíncrona, o 404. */
    @GetMapping("/operations/{operationId}")
    public OperationResponse getOperation(@PathVariable String operationId) {
        return lifecycleService.getOperation(operationId);
    }
}
