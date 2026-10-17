package co.edu.unicauca.userservice.dto;

/**
 * Respuesta de autenticación.
 *
 * @param authenticated true si las credenciales son correctas (200), false si no (401)
 * @param user          datos del usuario autenticado, o null cuando falla
 * @param message       mensaje descriptivo
 */
public record LoginResponse(boolean authenticated, UserDTO user, String message) {
}
