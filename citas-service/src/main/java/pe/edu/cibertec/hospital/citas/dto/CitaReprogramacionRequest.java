package pe.edu.cibertec.hospital.citas.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record CitaReprogramacionRequest(

        @NotNull
        @Future
        OffsetDateTime fechaHora

) {
}