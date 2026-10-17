package co.edu.unicauca.userservice.controller;

import co.edu.unicauca.userservice.dto.UserDTO;
import co.edu.unicauca.userservice.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Extensión necesaria para que Review Service verifique la elegibilidad del revisor
 * (que exista y que tenga el rol REVIEWER).
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** GET /users/{id} → UserDTO con roles, o 404 si no existe. */
    @GetMapping("/{id}")
    public UserDTO findById(@PathVariable Long id) {
        return userService.findById(id);
    }
}
