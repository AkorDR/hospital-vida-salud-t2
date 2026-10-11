package pe.edu.cibertec.hospital.pacientes.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.cibertec.hospital.pacientes.dto.PacienteRequest;
import pe.edu.cibertec.hospital.pacientes.model.Paciente;
import pe.edu.cibertec.hospital.pacientes.service.PacienteService;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

    private final PacienteService pacienteService;

    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Paciente> registrar(
            @Valid @RequestBody PacienteRequest request
    ) {
        return pacienteService.registrar(request);
    }
}