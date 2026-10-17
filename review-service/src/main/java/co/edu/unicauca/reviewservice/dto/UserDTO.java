package co.edu.unicauca.reviewservice.dto;

import java.util.Set;

/** Usuario tal como lo devuelve User Service (duplicado por servicio). */
public record UserDTO(Long id, String username, String fullName, String email, Set<String> roles) {
}
