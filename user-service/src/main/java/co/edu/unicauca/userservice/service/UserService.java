package co.edu.unicauca.userservice.service;

import co.edu.unicauca.userservice.dto.LoginResponse;
import co.edu.unicauca.userservice.dto.UserDTO;
import co.edu.unicauca.userservice.exception.ResourceNotFoundException;
import co.edu.unicauca.userservice.model.User;
import co.edu.unicauca.userservice.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Autentica un usuario comparando el hash almacenado.
     *
     * @return LoginResponse con authenticated=true o false (nunca lanza)
     */
    @Transactional(readOnly = true)
    public LoginResponse login(String username, String password) {
        Optional<User> found = userRepository.findByUsername(username);
        if (found.isEmpty() || !passwordEncoder.matches(password, found.get().getPassword())) {
            return new LoginResponse(false, null, "Credenciales inválidas");
        }
        return new LoginResponse(true, toDto(found.get()), "Autenticación exitosa");
    }

    @Transactional(readOnly = true)
    public UserDTO findById(Long id) {
        return userRepository.findById(id)
                .map(UserService::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario " + id + " no encontrado"));
    }

    private static UserDTO toDto(User user) {
        return new UserDTO(user.getId(), user.getUsername(), user.getFullName(), user.getEmail(), user.getRoles());
    }
}
