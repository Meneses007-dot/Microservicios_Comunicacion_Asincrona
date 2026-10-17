package co.edu.unicauca.reviewservice.controller;

import co.edu.unicauca.reviewservice.dto.AssignRequest;
import co.edu.unicauca.reviewservice.dto.AssignResponse;
import co.edu.unicauca.reviewservice.dto.ResultRequest;
import co.edu.unicauca.reviewservice.dto.ResultResponse;
import co.edu.unicauca.reviewservice.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * POST /reviews/assign → 201 con la asignación confirmada.
     * 409 si la pregunta no está en PENDING_REVIEW; 404 si el revisor no existe;
     * 422 si no tiene rol REVIEWER; 503 si Lifecycle falla (asignación en FAILED).
     */
    @PostMapping("/assign")
    public ResponseEntity<AssignResponse> assign(@Valid @RequestBody AssignRequest request) {
        AssignResponse response = reviewService.assign(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** POST /reviews/{questionId}/result → registra la revisión y transiciona el estado. */
    @PostMapping("/{questionId}/result")
    public ResultResponse registerResult(@PathVariable Long questionId,
                                         @Valid @RequestBody ResultRequest request) {
        return reviewService.registerResult(questionId, request);
    }
}
