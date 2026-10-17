package co.edu.unicauca.reviewservice.client;

import co.edu.unicauca.reviewservice.dto.LifecycleStateDTO;
import co.edu.unicauca.reviewservice.dto.TransitionPayload;
import co.edu.unicauca.reviewservice.exception.ConflictException;
import co.edu.unicauca.reviewservice.exception.ServiceUnavailableException;
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
 * Cliente síncrono hacia Lifecycle Service: Review Service nunca cambia el estado
 * por su cuenta, solo lo consulta y solicita transiciones.
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

    /**
     * Solicita una transición.
     *
     * @throws ConflictException          si Lifecycle responde 409 (transición no permitida)
     * @throws ServiceUnavailableException si Lifecycle no responde
     */
    public void transition(Long questionId, String targetState, String requestedBy, String comment) {
        String url = baseUrl + "/lifecycle/transition";
        TransitionPayload payload = new TransitionPayload(questionId, targetState, requestedBy, comment);
        try {
            restTemplate.postForEntity(url, payload, Void.class);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.CONFLICT) {
                throw new ConflictException("Lifecycle rechazó la transición a " + targetState
                        + " para la pregunta " + questionId);
            }
            throw new ServiceUnavailableException("Lifecycle rechazó la transición: " + ex.getMessage(), ex);
        } catch (RestClientException ex) {
            log.error("Fallo al solicitar transición a Lifecycle en {}: {}", url, ex.getMessage());
            throw new ServiceUnavailableException("El servicio de ciclo de vida no está disponible.", ex);
        }
    }
}
