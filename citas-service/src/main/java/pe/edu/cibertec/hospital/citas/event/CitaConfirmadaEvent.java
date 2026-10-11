package pe.edu.cibertec.hospital.citas.event;

import java.time.OffsetDateTime;

public record CitaConfirmadaEvent(
        String eventoId,
        Long citaId,
        Long pacienteId,
        String especialidad,
        OffsetDateTime fechaHora,
        String estadoAnterior,
        String estadoNuevo,
        OffsetDateTime fechaEvento,
        String origen
) {
}