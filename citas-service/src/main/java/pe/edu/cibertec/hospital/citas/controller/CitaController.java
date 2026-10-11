package pe.edu.cibertec.hospital.citas.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.cibertec.hospital.citas.dto.CitaReprogramacionRequest;
import pe.edu.cibertec.hospital.citas.dto.CitaRequest;
import pe.edu.cibertec.hospital.citas.model.Cita;
import pe.edu.cibertec.hospital.citas.service.CitaService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    private final CitaService service;

    public CitaController(CitaService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Cita> registrar(
            @Valid @RequestBody CitaRequest request
    ) {
        return service.registrar(request);
    }

    @GetMapping
    public Flux<Cita> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Mono<Cita> buscarPorId(
            @PathVariable("id") Long id
    ) {
        return service.buscarPorId(id);
    }

    @GetMapping("/paciente/{pacienteId}")
    public Flux<Cita> buscarPorPaciente(
            @PathVariable("pacienteId") Long pacienteId
    ) {
        return service.buscarPorPaciente(pacienteId);
    }

    @PatchMapping("/{id}/confirmar")
    public Mono<Cita> confirmar(
            @PathVariable("id") Long id
    ) {
        return service.confirmar(id);
    }

    @PatchMapping("/{id}/reprogramar")
    public Mono<Cita> reprogramar(
            @PathVariable("id") Long id,
            @Valid @RequestBody CitaReprogramacionRequest request
    ) {
        return service.reprogramar(id, request);
    }

    @PatchMapping("/{id}/cancelar")
    public Mono<Cita> cancelar(
            @PathVariable("id") Long id
    ) {
        return service.cancelar(id);
    }
}