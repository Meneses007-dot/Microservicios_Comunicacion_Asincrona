package co.edu.unicauca.questionservice.client;

import co.edu.unicauca.questionservice.dto.LifecycleOperationDTO;
import co.edu.unicauca.questionservice.dto.LifecycleStateDTO;
import co.edu.unicauca.questionservice.dto.TransitionPayload;
import co.edu.unicauca.questionservice.exception.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

/**
 * Cliente síncrono hacia Lifecycle Service. Question Service nunca modifica el
 * estado: solo <b>solicita</b> transiciones (por ejemplo REJECTED → DRAFT tras
 * corregir una pregunta) y consulta resultados de operaciones.
 */
@Component
public class LifecycleClient {

    private static final Logger log = LoggerFactory.getLogger(LifecycleClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public LifecycleClient(RestTemplate restTemplate,
                           @Value("${lifecycle.service.url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    /** Estado actual, o vacío si Lifecycle no tiene registro (404). */
    public Optional<LifecycleStateDTO> getState(Long questionId) {
        String url = baseUrl + "/lifecycle/" + questionId;
        try {
            ResponseEntity<LifecycleStateDTO> response =
                    restTemplate.getForEntity(url, LifecycleStateDTO.class);
            return Optional.ofNullable(response.getBody());
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                return Optional.empty();
            }
            throw new ServiceUnavailableException("Lifecycle rechazó la consulta de estado: " + ex.getMessage(), ex);
        } catch (RestClientException ex) {
            log.error("Lifecycle Service no disponible en {}: {}", url, ex.getMessage());
            throw new ServiceUnavailableException("El servicio de ciclo de vida no está disponible.", ex);
        }
    }

    public void transition(Long questionId, String targetState, String requestedBy, String comment) {
        String url = baseUrl + "/lifecycle/transition";
        TransitionPayload payload = new TransitionPayload(questionId, targetState, requestedBy, comment);
        try {
            restTemplate.postForEntity(url, payload, Void.class);
        } catch (RestClientException ex) {
            log.error("Fallo al solicitar transición a Lifecycle en {}: {}", url, ex.getMessage());
            throw new ServiceUnavailableException(
                    "No fue posible aplicar la transición a " + targetState + ": " + ex.getMessage(), ex);
        }
    }

    /** Resultado de la operación, o vacío si Lifecycle aún no lo tiene (404). */
    public Optional<LifecycleOperationDTO> getOperation(String operationId) {
        String url = baseUrl + "/lifecycle/operations/" + operationId;
        try {
            ResponseEntity<LifecycleOperationDTO> response =
                    restTemplate.getForEntity(url, LifecycleOperationDTO.class);
            return Optional.ofNullable(response.getBody());
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                return Optional.empty();
            }
            throw new ServiceUnavailableException("Lifecycle rechazó la consulta de operación: " + ex.getMessage(), ex);
        } catch (RestClientException ex) {
            log.error("Lifecycle Service no disponible en {}: {}", url, ex.getMessage());
            throw new ServiceUnavailableException("El servicio de ciclo de vida no está disponible.", ex);
        }
    }
}
