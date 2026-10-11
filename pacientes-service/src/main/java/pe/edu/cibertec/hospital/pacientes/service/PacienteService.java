package pe.edu.cibertec.hospital.pacientes.service;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.cibertec.hospital.pacientes.dto.PacienteRequest;
import pe.edu.cibertec.hospital.pacientes.dto.PacienteUpdateRequest;
import pe.edu.cibertec.hospital.pacientes.exception.PacienteDuplicadoException;
import pe.edu.cibertec.hospital.pacientes.model.Paciente;
import pe.edu.cibertec.hospital.pacientes.repository.PacienteRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;

@Service
public class PacienteService {

    private final PacienteRepository repository;

    public PacienteService(PacienteRepository repository) {
        this.repository = repository;
    }

    public Mono<Paciente> registrar(PacienteRequest request) {
        return Mono.defer(() -> {
            String tipo = request.tipoDocumento()
                    .strip().toUpperCase(Locale.ROOT);
            String numero = request.numeroDocumento()
                    .strip().toUpperCase(Locale.ROOT);

            return repository.existsByTipoDocumentoAndNumeroDocumento(
                    tipo, numero
            ).flatMap(existe -> {
                if (existe) {
                    return Mono.<Paciente>error(
                            new PacienteDuplicadoException()
                    );
                }

                Paciente paciente = new Paciente();
                paciente.setTipoDocumento(tipo);
                paciente.setNumeroDocumento(numero);
                paciente.setNombres(request.nombres().strip());
                paciente.setApellidos(request.apellidos().strip());
                paciente.setFechaNacimiento(request.fechaNacimiento());
                paciente.setTelefono(request.telefono().strip());
                paciente.setCorreo(request.correo().strip());
                paciente.setDireccion(request.direccion() == null
                        ? null : request.direccion().strip());
                paciente.setFechaRegistro(
                        OffsetDateTime.now(ZoneOffset.UTC)
                );
                paciente.setEstado("ACTIVO");

                return repository.save(paciente)
                        .onErrorMap(
                                DuplicateKeyException.class,
                                error -> new PacienteDuplicadoException()
                        );
            });
        });
    }

    public Flux<Paciente> listar() {
        return repository.findAll();
    }

    public Mono<Paciente> buscarPorId(Long id) {
        return repository.findById(id)
                .switchIfEmpty(noEncontrado());
    }

    public Mono<Paciente> buscarPorDocumento(
            String tipo, String numero
    ) {
        return repository.findByTipoDocumentoAndNumeroDocumento(
                tipo.strip().toUpperCase(Locale.ROOT),
                numero.strip().toUpperCase(Locale.ROOT)
        ).switchIfEmpty(noEncontrado());
    }

    public Mono<Paciente> actualizar(
            Long id, PacienteUpdateRequest request
    ) {
        return buscarPorId(id).flatMap(paciente -> {
            paciente.setNombres(request.nombres().strip());
            paciente.setApellidos(request.apellidos().strip());
            paciente.setFechaNacimiento(request.fechaNacimiento());
            paciente.setTelefono(request.telefono().strip());
            paciente.setCorreo(request.correo().strip());
            paciente.setDireccion(request.direccion() == null
                    ? null : request.direccion().strip());
            paciente.setEstado(request.estado());

            return repository.save(paciente);
        });
    }

    private Mono<Paciente> noEncontrado() {
        return Mono.error(new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Paciente no encontrado"
        ));
    }
}