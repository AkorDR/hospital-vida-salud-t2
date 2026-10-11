package pe.edu.cibertec.hospital.pacientes.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.cibertec.hospital.pacientes.dto.PacienteRequest;
import pe.edu.cibertec.hospital.pacientes.dto.PacienteUpdateRequest;
import pe.edu.cibertec.hospital.pacientes.model.Paciente;
import pe.edu.cibertec.hospital.pacientes.service.PacienteService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

    private final PacienteService service;

    public PacienteController(PacienteService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Paciente> registrar(
            @Valid @RequestBody PacienteRequest request
    ) {
        return service.registrar(request);
    }

    @GetMapping
    public Flux<Paciente> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Mono<Paciente> buscarPorId(
            @PathVariable("id") Long id
    ) {
        return service.buscarPorId(id);
    }

    @GetMapping("/documento")
    public Mono<Paciente> buscarPorDocumento(
            @RequestParam("tipo") String tipo,
            @RequestParam("numero") String numero
    ) {
        return service.buscarPorDocumento(tipo, numero);
    }

    @PutMapping("/{id}")
    public Mono<Paciente> actualizar(
            @PathVariable("id") Long id,
            @Valid @RequestBody PacienteUpdateRequest request
    ) {
        return service.actualizar(id, request);
    }
}