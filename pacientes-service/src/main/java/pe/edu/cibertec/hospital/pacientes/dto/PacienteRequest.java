package pe.edu.cibertec.hospital.pacientes.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PacienteRequest(

        @NotBlank(message = "El tipo de documento es obligatorio")
        @Size(max = 20)
        String tipoDocumento,

        @NotBlank(message = "El número de documento es obligatorio")
        @Size(max = 20)
        String numeroDocumento,

        @NotBlank(message = "Los nombres son obligatorios")
        @Size(max = 100)
        String nombres,

        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(max = 100)
        String apellidos,

        @NotNull(message = "La fecha de nacimiento es obligatoria")
        @PastOrPresent(message = "La fecha de nacimiento no puede ser futura")
        LocalDate fechaNacimiento,

        @NotBlank(message = "El teléfono es obligatorio")
        @Size(max = 20)
        String telefono,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo debe tener un formato válido")
        @Size(max = 150)
        String correo,

        @Size(max = 250)
        String direccion

) {
}