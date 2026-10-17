package co.edu.unicauca.questionservice.client;

import co.edu.unicauca.questionservice.dto.ValidationRequest;
import co.edu.unicauca.questionservice.dto.ValidationResult;
import co.edu.unicauca.questionservice.exception.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Cliente síncrono hacia Validation Service.
 * Si el servicio no responde, se lanza ServiceUnavailableException (503)
 * y la pregunta <b>no</b> se persiste.
 */
@Component
public class ValidationClient {

    private static final Logger log = LoggerFactory.getLogger(ValidationClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public ValidationClient(RestTemplate restTemplate,
                            @Value("${validation.service.url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public ValidationResult validate(ValidationRequest request) {
        String url = baseUrl + "/validate";
        try {
            ResponseEntity<ValidationResult> response =
                    restTemplate.postForEntity(url, request, ValidationResult.class);
            ValidationResult body = response.getBody();
            if (body == null) {
                throw new ServiceUnavailableException("Validation Service devolvió una respuesta vacía");
            }
            return body;
        } catch (RestClientException ex) {
            log.error("Fallo al invocar Validation Service en {}: {}", url, ex.getMessage());
            throw new ServiceUnavailableException(
                    "El servicio de validación no está disponible. Intente más tarde.", ex);
        }
    }
}
