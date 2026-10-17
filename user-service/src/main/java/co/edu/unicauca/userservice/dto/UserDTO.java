package co.edu.unicauca.userservice.dto;

import java.util.Set;

/** Representación pública de un usuario (sin la contraseña). */
public record UserDTO(
        Long id,
        String username,
        String fullName,
        String email,
        Set<String> roles) {
}
