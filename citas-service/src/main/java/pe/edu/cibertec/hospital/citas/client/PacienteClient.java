package pe.edu.cibertec.hospital.citas.client;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Component
public class PacienteClient {

    private final WebClient webClient;

    public PacienteClient(@LoadBalanced WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl("http://pacientes-service")
                .build();
    }

    public Mono<Void> validarExistencia(Long pacienteId) {
        return webClient.get()
                .uri("/api/pacientes/{id}", pacienteId)
                .retrieve()
                .onStatus(
                        status -> status.value() == 404,
                        response -> response.releaseBody()
                                .then(Mono.error(
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "El paciente indicado no existe"
                                        )
                                ))
                )
                .onStatus(
                        status -> status.isError(),
                        response -> response.releaseBody()
                                .then(Mono.error(
                                        new ResponseStatusException(
                                                HttpStatus.SERVICE_UNAVAILABLE,
                                                "No se pudo validar al paciente"
                                        )
                                ))
                )
                .toBodilessEntity()
                .timeout(Duration.ofSeconds(5))
                .onErrorMap(
                        TimeoutException.class,
                        error -> new ResponseStatusException(
                                HttpStatus.GATEWAY_TIMEOUT,
                                "El servicio de pacientes tardó demasiado",
                                error
                        )
                )
                .onErrorMap(
                        WebClientRequestException.class,
                        error -> new ResponseStatusException(
                                HttpStatus.SERVICE_UNAVAILABLE,
                                "El servicio de pacientes no está disponible",
                                error
                        )
                )
                .then();
    }
}