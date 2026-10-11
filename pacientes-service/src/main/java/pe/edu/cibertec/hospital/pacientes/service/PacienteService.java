package pe.edu.cibertec.hospital.pacientes.service;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import pe.edu.cibertec.hospital.pacientes.dto.PacienteRequest;
import pe.edu.cibertec.hospital.pacientes.exception.PacienteDuplicadoException;
import pe.edu.cibertec.hospital.pacientes.model.Paciente;
import pe.edu.cibertec.hospital.pacientes.repository.PacienteRepository;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;

@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;

    public PacienteService(PacienteRepository pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
    }

    public Mono<Paciente> registrar(PacienteRequest request) {
        return Mono.defer(() -> {
            String tipoDocumento = request.tipoDocumento()
                    .strip().toUpperCase(Locale.ROOT);

            String numeroDocumento = request.numeroDocumento()
                    .strip().toUpperCase(Locale.ROOT);

            return pacienteRepository
                    .existsByTipoDocumentoAndNumeroDocumento(
                            tipoDocumento, numeroDocumento
                    )
                    .flatMap(existe -> {
                        if (existe) {
                            return Mono.<Paciente>error(
                                    new PacienteDuplicadoException()
                            );
                        }

                        Paciente paciente = new Paciente();
                        paciente.setTipoDocumento(tipoDocumento);
                        paciente.setNumeroDocumento(numeroDocumento);
                        paciente.setNombres(request.nombres().strip());
                        paciente.setApellidos(request.apellidos().strip());
                        paciente.setFechaNacimiento(request.fechaNacimiento());
                        paciente.setTelefono(request.telefono().strip());
                        paciente.setCorreo(request.correo().strip());

                        paciente.setDireccion(
                                request.direccion() == null
                                        ? null
                                        : request.direccion().strip()
                        );

                        paciente.setFechaRegistro(
                                OffsetDateTime.now(ZoneOffset.UTC)
                        );
                        paciente.setEstado("ACTIVO");

                        return pacienteRepository.save(paciente)
                                .onErrorMap(
                                        DuplicateKeyException.class,
                                        error -> new PacienteDuplicadoException()
                                );
                    });
        });
    }
}