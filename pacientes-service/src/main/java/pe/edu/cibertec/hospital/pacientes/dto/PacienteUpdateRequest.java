package pe.edu.cibertec.hospital.pacientes.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record PacienteUpdateRequest(
        @NotBlank @Size(max = 100) String nombres,
        @NotBlank @Size(max = 100) String apellidos,
        @NotNull @PastOrPresent LocalDate fechaNacimiento,
        @NotBlank @Size(max = 20) String telefono,
        @NotBlank @Email @Size(max = 150) String correo,
        @Size(max = 250) String direccion,
        @NotBlank @Pattern(regexp = "ACTIVO|INACTIVO") String estado
) {
}