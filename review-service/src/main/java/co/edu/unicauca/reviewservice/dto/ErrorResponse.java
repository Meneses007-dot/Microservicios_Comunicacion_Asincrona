package co.edu.unicauca.reviewservice.dto;

import java.util.List;

/** Error uniforme. */
public record ErrorResponse(String message, List<String> errors) {

    public static ErrorResponse of(String message, List<String> errors) {
        return new ErrorResponse(message, errors == null ? List.of() : errors);
    }
}
