package co.edu.unicauca.validationservice.controller;

import co.edu.unicauca.validationservice.dto.ValidationRequest;
import co.edu.unicauca.validationservice.dto.ValidationResult;
import co.edu.unicauca.validationservice.service.ValidationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/validate")
public class ValidationController {

    private final ValidationService validationService;

    public ValidationController(ValidationService validationService) {
        this.validationService = validationService;
    }

    /**
     * POST /validate → 200 {@code {valid, errors}}.
     * Los fallos de negocio NO son 400: solo el JSON malformado lo es (ver el handler).
     */
    @PostMapping
    public ValidationResult validate(@RequestBody ValidationRequest request) {
        return validationService.validate(request);
    }
}
