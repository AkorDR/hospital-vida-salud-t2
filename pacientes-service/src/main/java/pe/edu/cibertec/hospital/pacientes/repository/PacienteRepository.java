package pe.edu.cibertec.hospital.pacientes.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import pe.edu.cibertec.hospital.pacientes.model.Paciente;
import reactor.core.publisher.Mono;

public interface PacienteRepository
        extends ReactiveCrudRepository<Paciente, Long> {

    Mono<Boolean> existsByTipoDocumentoAndNumeroDocumento(
            String tipoDocumento,
            String numeroDocumento
    );

    Mono<Paciente> findByTipoDocumentoAndNumeroDocumento(
            String tipoDocumento,
            String numeroDocumento
    );
}