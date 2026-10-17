package co.edu.unicauca.validationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Validation Service (:8082).
 *
 * <p>Servicio sin estado (no tiene base de datos): recibe el enunciado, las
 * opciones y la clave, y responde {@code {valid, errors}}. Un fallo de negocio
 * se responde con 200 y {@code valid:false}; solo un JSON malformado produce 400.</p>
 */
@SpringBootApplication
public class ValidationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ValidationServiceApplication.class, args);
    }
}
