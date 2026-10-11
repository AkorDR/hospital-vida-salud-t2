package pe.edu.cibertec.hospital.citas.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.cibertec.hospital.citas.client.PacienteClient;
import pe.edu.cibertec.hospital.citas.dto.CitaReprogramacionRequest;
import pe.edu.cibertec.hospital.citas.dto.CitaRequest;
import pe.edu.cibertec.hospital.citas.event.CitaConfirmadaEvent;
import pe.edu.cibertec.hospital.citas.event.CitaEventPublisher;
import pe.edu.cibertec.hospital.citas.model.Cita;
import pe.edu.cibertec.hospital.citas.repository.CitaRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.UUID;

@Service
public class CitaService {

    private static final Logger log =
            LoggerFactory.getLogger(CitaService.class);

    private final CitaRepository repository;
    private final PacienteClient pacienteClient;
    private final CitaEventPublisher eventPublisher;

    public CitaService(
            CitaRepository repository,
            PacienteClient pacienteClient,
            CitaEventPublisher eventPublisher
    ) {
        this.repository = repository;
        this.pacienteClient = pacienteClient;
        this.eventPublisher = eventPublisher;
    }

    public Mono<Cita> registrar(CitaRequest request) {
        return pacienteClient.validarExistencia(request.pacienteId())
                .then(Mono.defer(() -> {
                    OffsetDateTime ahora =
                            OffsetDateTime.now(ZoneOffset.UTC);

                    Cita cita = new Cita();
                    cita.setPacienteId(request.pacienteId());
                    cita.setEspecialidad(request.especialidad().strip());
                    cita.setFechaHora(request.fechaHora());
                    cita.setProfesionalCodigo(
                            request.profesionalCodigo()
                                    .strip()
                                    .toUpperCase(Locale.ROOT)
                    );
                    cita.setMotivo(request.motivo().strip());
                    cita.setEstado("PENDIENTE");
                    cita.setFechaCreacion(ahora);
                    cita.setFechaActualizacion(ahora);

                    return repository.save(cita);
                }))
                .onErrorMap(
                        DuplicateKeyException.class,
                        error -> new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "El paciente o profesional ya tiene una cita en ese horario",
                                error
                        )
                );
    }

    public Flux<Cita> listar() {
        return repository.findAll();
    }

    public Mono<Cita> buscarPorId(Long id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Cita no encontrada"
                        )
                ));
    }

    public Flux<Cita> buscarPorPaciente(Long pacienteId) {
        return repository.findByPacienteId(pacienteId);
    }

    @Transactional
    public Mono<Cita> confirmar(Long id) {
        return buscarPorId(id)
                .flatMap(cita -> {
                    if (!"PENDIENTE".equals(cita.getEstado())) {
                        return Mono.<Cita>error(
                                new ResponseStatusException(
                                        HttpStatus.CONFLICT,
                                        "Solo se puede confirmar una cita PENDIENTE"
                                )
                        );
                    }

                    OffsetDateTime ahora =
                            OffsetDateTime.now(ZoneOffset.UTC);

                    if (!cita.getFechaHora().isAfter(ahora)) {
                        return Mono.<Cita>error(
                                new ResponseStatusException(
                                        HttpStatus.CONFLICT,
                                        "No se puede confirmar una cita pasada"
                                )
                        );
                    }

                    String estadoAnterior = cita.getEstado();
                    cita.setEstado("CONFIRMADA");
                    cita.setFechaActualizacion(ahora);

                    return repository.save(cita)
                            .flatMap(guardada -> {
                                CitaConfirmadaEvent evento =
                                        new CitaConfirmadaEvent(
                                                UUID.randomUUID().toString(),
                                                guardada.getId(),
                                                guardada.getPacienteId(),
                                                guardada.getEspecialidad(),
                                                guardada.getFechaHora(),
                                                estadoAnterior,
                                                guardada.getEstado(),
                                                ahora,
                                                "citas-service"
                                        );

                                return eventPublisher.publicar(evento)
                                        .thenReturn(guardada);
                            });
                });
    }

    public Mono<Cita> reprogramar(
            Long id,
            CitaReprogramacionRequest request
    ) {
        return buscarPorId(id)
                .flatMap(cita -> {
                    if (!"PENDIENTE".equals(cita.getEstado())
                            && !"CONFIRMADA".equals(cita.getEstado())) {
                        return Mono.<Cita>error(
                                new ResponseStatusException(
                                        HttpStatus.CONFLICT,
                                        "Solo se pueden reprogramar citas PENDIENTES o CONFIRMADAS"
                                )
                        );
                    }

                    String estadoAnterior = cita.getEstado();
                    OffsetDateTime horarioAnterior = cita.getFechaHora();

                    cita.setFechaHora(request.fechaHora());
                    cita.setEstado("PENDIENTE");
                    cita.setFechaActualizacion(
                            OffsetDateTime.now(ZoneOffset.UTC)
                    );

                    return repository.save(cita)
                            .doOnNext(guardada -> log.info(
                                    "REPROGRAMACION: cita={}, estadoAnterior={}, "
                                            + "estadoNuevo={}, horarioAnterior={}, "
                                            + "horarioNuevo={}, fecha={}, origen=citas-service",
                                    guardada.getId(),
                                    estadoAnterior,
                                    guardada.getEstado(),
                                    horarioAnterior,
                                    guardada.getFechaHora(),
                                    guardada.getFechaActualizacion()
                            ));
                })
                .onErrorMap(
                        DuplicateKeyException.class,
                        error -> new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "El paciente o profesional ya tiene una cita en ese horario",
                                error
                        )
                );
    }

    public Mono<Cita> cancelar(Long id) {
        return buscarPorId(id)
                .flatMap(cita -> {
                    if (!"PENDIENTE".equals(cita.getEstado())
                            && !"CONFIRMADA".equals(cita.getEstado())) {
                        return Mono.<Cita>error(
                                new ResponseStatusException(
                                        HttpStatus.CONFLICT,
                                        "Solo se pueden cancelar citas PENDIENTES o CONFIRMADAS"
                                )
                        );
                    }

                    String estadoAnterior = cita.getEstado();

                    cita.setEstado("CANCELADA");
                    cita.setFechaActualizacion(
                            OffsetDateTime.now(ZoneOffset.UTC)
                    );

                    return repository.save(cita)
                            .doOnNext(guardada -> log.info(
                                    "CANCELACION: cita={}, estadoAnterior={}, "
                                            + "estadoNuevo={}, fecha={}, origen=citas-service",
                                    guardada.getId(),
                                    estadoAnterior,
                                    guardada.getEstado(),
                                    guardada.getFechaActualizacion()
                            ));
                });
    }
}