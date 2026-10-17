package co.edu.unicauca.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * User Service (:8081).
 *
 * <p>Responsabilidad única: autenticar usuarios y verificar roles.
 * No implementa JWT (queda para el corte 3); solo valida credenciales
 * contra su propia base de datos con contraseñas almacenadas como hash.</p>
 */
@SpringBootApplication
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
