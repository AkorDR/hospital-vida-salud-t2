package pe.edu.cibertec.hospital.notificaciones.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import pe.edu.cibertec.hospital.notificaciones.config.RabbitConfig;
import pe.edu.cibertec.hospital.notificaciones.event.CitaConfirmadaEvent;

@Component
public class CitaEventConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(CitaEventConsumer.class);

    @RabbitListener(queues = RabbitConfig.COLA_NOTIFICACIONES)
    public void notificar(CitaConfirmadaEvent evento) {
        log.info(
                "NOTIFICACION SIMULADA: evento={}, cita={}, paciente={}, "
                        + "especialidad={}, horario={}, estado={}",
                evento.eventoId(),
                evento.citaId(),
                evento.pacienteId(),
                evento.especialidad(),
                evento.fechaHora(),
                evento.estadoNuevo()
        );
    }

    @RabbitListener(queues = RabbitConfig.COLA_AUDITORIA)
    public void auditar(CitaConfirmadaEvent evento) {
        log.info(
                "AUDITORIA: evento={}, recurso=cita, id={}, "
                        + "estadoAnterior={}, estadoNuevo={}, fecha={}, origen={}",
                evento.eventoId(),
                evento.citaId(),
                evento.estadoAnterior(),
                evento.estadoNuevo(),
                evento.fechaEvento(),
                evento.origen()
        );
    }
}