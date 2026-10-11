package pe.edu.cibertec.hospital.pacientes.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class PacienteDuplicadoException extends RuntimeException {

    public PacienteDuplicadoException() {
        super("Ya existe un paciente con ese tipo y número de documento");
    }
}