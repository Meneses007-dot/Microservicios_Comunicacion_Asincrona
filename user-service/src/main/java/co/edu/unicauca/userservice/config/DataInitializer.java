package co.edu.unicauca.userservice.config;

import co.edu.unicauca.userservice.model.User;
import co.edu.unicauca.userservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Carga usuarios de prueba con contraseña hasheada (BCrypt).
 * Se ejecuta solo si la base está vacía, para no duplicar en reinicios.
 *
 * <p>Credenciales de demostración (todas con la misma clave: <b>secret123</b>):
 * <ul>
 *   <li>{@code teacher} / TEACHER</li>
 *   <li>{@code admin}   / ADMIN</li>
 *   <li>{@code reviewer} / REVIEWER</li>
 *   <li>{@code reviewer2} / REVIEWER (segundo revisor para pruebas)</li>
 * </ul>
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private static final String DEFAULT_PASSWORD = "secret123";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }
        String hash = passwordEncoder.encode(DEFAULT_PASSWORD);
        userRepository.save(new User("teacher", hash, "Docente de prueba", "teacher@saberpro.edu.co", Set.of("TEACHER")));
        userRepository.save(new User("admin", hash, "Administrador de prueba", "admin@saberpro.edu.co", Set.of("ADMIN")));
        userRepository.save(new User("reviewer", hash, "Revisor Uno", "reviewer1@saberpro.edu.co", Set.of("REVIEWER")));
        userRepository.save(new User("reviewer2", hash, "Revisor Dos", "reviewer2@saberpro.edu.co", Set.of("REVIEWER")));
        log.info("Usuarios de prueba cargados. Clave para todos: {}", DEFAULT_PASSWORD);
    }
}
