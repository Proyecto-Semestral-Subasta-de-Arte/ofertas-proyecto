package cl.sda1085.ofertas.webclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class SubastaClient {

    private final WebClient webClient;

    //Inyección dinámica de la URL del microservicio de subastas desde el archivo YAML
    public SubastaClient(WebClient.Builder webClientBuilder,
                         @Value("${subastas-service.url}") String urlSubastas) {
        this.webClient = webClientBuilder.baseUrl(urlSubastas).build();
    }

    //Consulta si la subasta existe y obtiene sus datos en tiempo real
    public Map<String, Object> obtenerSubastaPorId(Long id) {
        return this.webClient.get()
                .uri("/{id}", id)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(),
                        response -> Mono.error(new RuntimeException("La subasta con ID " + id + " no existe o ya no se encuentra disponible.")))
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {})
                .block();  //Bloqueo síncronizado para asegurar la regla de negocio
    }
}
