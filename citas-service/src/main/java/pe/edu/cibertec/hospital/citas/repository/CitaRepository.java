package pe.edu.cibertec.hospital.citas.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import pe.edu.cibertec.hospital.citas.model.Cita;
import reactor.core.publisher.Flux;

public interface CitaRepository
        extends ReactiveCrudRepository<Cita, Long> {

    Flux<Cita> findByPacienteId(Long pacienteId);
}