package co.edu.unicauca.reviewservice.client;

import co.edu.unicauca.reviewservice.dto.UserDTO;
import co.edu.unicauca.reviewservice.exception.ResourceNotFoundException;
import co.edu.unicauca.reviewservice.exception.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/** Cliente síncrono hacia User Service para verificar la elegibilidad del revisor. */
@Component
public class UserClient {

    private static final Logger log = LoggerFactory.getLogger(UserClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public UserClient(RestTemplate restTemplate,
                      @Value("${user.service.url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    /**
     * @throws ResourceNotFoundException  si el usuario no existe (404)
     * @throws ServiceUnavailableException si User Service no responde (503)
     */
    public UserDTO getUser(Long userId) {
        String url = baseUrl + "/users/" + userId;
        try {
            return restTemplate.getForObject(url, UserDTO.class);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new ResourceNotFoundException("El usuario " + userId + " no existe");
            }
            throw new ServiceUnavailableException("User Service rechazó la consulta: " + ex.getMessage(), ex);
        } catch (RestClientException ex) {
            log.error("User Service no disponible en {}: {}", url, ex.getMessage());
            throw new ServiceUnavailableException("El servicio de usuarios no está disponible.", ex);
        }
    }
}
