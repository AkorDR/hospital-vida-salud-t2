package pe.edu.cibertec.hospital.citas.event;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.cibertec.hospital.citas.config.RabbitConfig;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class CitaEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public CitaEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public Mono<Void> publicar(CitaConfirmadaEvent evento) {
        return Mono.<Void>fromRunnable(() ->
                        rabbitTemplate.convertAndSend(
                                RabbitConfig.EXCHANGE,
                                RabbitConfig.ROUTING_KEY,
                                evento
                        )
                )
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorMap(
                        AmqpException.class,
                        error -> new ResponseStatusException(
                                HttpStatus.SERVICE_UNAVAILABLE,
                                "No se pudo publicar el evento en RabbitMQ",
                                error
                        )
                );
    }
}