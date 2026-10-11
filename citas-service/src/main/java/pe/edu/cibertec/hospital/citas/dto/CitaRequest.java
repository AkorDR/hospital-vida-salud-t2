package pe.edu.cibertec.hospital.citas.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record CitaRequest(

        @NotNull
        @Positive
        Long pacienteId,

        @NotBlank
        @Size(max = 100)
        String especialidad,

        @NotNull
        @Future
        OffsetDateTime fechaHora,

        @NotBlank
        @Size(max = 50)
        String profesionalCodigo,

        @NotBlank
        @Size(max = 250)
        String motivo

) {
}