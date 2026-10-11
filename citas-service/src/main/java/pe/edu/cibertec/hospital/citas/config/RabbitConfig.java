package pe.edu.cibertec.hospital.citas.config;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "hospital.eventos";
    public static final String ROUTING_KEY = "cita.confirmada";

    public static final String COLA_NOTIFICACIONES =
            "hospital.notificaciones";

    public static final String COLA_AUDITORIA =
            "hospital.auditoria";

    @Bean
    public Declarables configurarRabbit() {
        TopicExchange exchange =
                new TopicExchange(EXCHANGE, true, false);

        Queue notificaciones =
                new Queue(COLA_NOTIFICACIONES, true);

        Queue auditoria =
                new Queue(COLA_AUDITORIA, true);

        return new Declarables(
                exchange,
                notificaciones,
                auditoria,
                BindingBuilder.bind(notificaciones)
                        .to(exchange)
                        .with(ROUTING_KEY),
                BindingBuilder.bind(auditoria)
                        .to(exchange)
                        .with(ROUTING_KEY)
        );
    }
}