package co.edu.unicauca.questionservice.dto;

import java.util.List;

/** Error uniforme con la lista de motivos. */
public record ErrorResponse(String message, List<String> errors) {

    public static ErrorResponse of(String message, List<String> errors) {
        return new ErrorResponse(message, errors == null ? List.of() : errors);
    }
}
