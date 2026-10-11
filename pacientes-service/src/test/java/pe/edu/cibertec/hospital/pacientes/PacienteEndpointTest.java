package pe.edu.cibertec.hospital.pacientes;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import pe.edu.cibertec.hospital.pacientes.dto.PacienteRequest;
import pe.edu.cibertec.hospital.pacientes.model.Paciente;
import pe.edu.cibertec.hospital.pacientes.repository.PacienteRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class PacienteEndpointTest {

    private static final String ENDPOINT = "/api/pacientes";
    private static final Duration ESPERA = Duration.ofSeconds(10);

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private PacienteRepository pacienteRepository;

    private String numeroDocumento;

    @BeforeEach
    void prepararDocumento() {
        numeroDocumento = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 20)
                .toUpperCase(java.util.Locale.ROOT);
    }

    @AfterEach
    void limpiarPacienteDePrueba() {
        pacienteRepository
                .findByTipoDocumentoAndNumeroDocumento("PRUEBA", numeroDocumento)
                .flatMap(pacienteRepository::delete)
                .block(ESPERA);
    }

    @Test
    void registrarPacienteValidoDevuelve201YGuardaEnBaseDeDatos() {
        Paciente respuesta = webTestClient.post()
                .uri(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(crearRequest("Paciente"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Paciente.class)
                .returnResult()
                .getResponseBody();

        assertThat(respuesta).isNotNull();
        assertThat(respuesta.getId()).isPositive();
        assertThat(respuesta.getEstado()).isEqualTo("ACTIVO");
        assertThat(respuesta.getFechaRegistro()).isNotNull();
        assertThat(respuesta.getNumeroDocumento())
                .isEqualTo(numeroDocumento);

        Paciente guardado = pacienteRepository
                .findByTipoDocumentoAndNumeroDocumento("PRUEBA", numeroDocumento)
                .block(ESPERA);

        assertThat(guardado).isNotNull();
        assertThat(guardado.getId()).isEqualTo(respuesta.getId());
        assertThat(guardado.getNombres()).isEqualTo("Paciente");
    }

    @Test
    void registrarDocumentoDuplicadoDevuelve409YSoloGuardaUnPaciente() {
        PacienteRequest request = crearRequest("Paciente");

        webTestClient.post()
                .uri(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated();

        webTestClient.post()
                .uri(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(409);

        Long cantidad = pacienteRepository.findAll()
                .filter(paciente ->
                        "PRUEBA".equals(paciente.getTipoDocumento())
                                && numeroDocumento.equals(paciente.getNumeroDocumento()))
                .count()
                .block(ESPERA);

        assertThat(cantidad).isEqualTo(1L);
    }

    @Test
    void registrarNombreVacioDevuelve400YNoGuardaPaciente() {
        webTestClient.post()
                .uri(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(crearRequest(""))
                .exchange()
                .expectStatus().isBadRequest();

        Boolean existe = pacienteRepository
                .existsByTipoDocumentoAndNumeroDocumento("PRUEBA", numeroDocumento)
                .block(ESPERA);

        assertThat(existe).isFalse();
    }

    private PacienteRequest crearRequest(String nombres) {
        return new PacienteRequest(
                "PRUEBA",
                numeroDocumento,
                nombres,
                "De Prueba",
                LocalDate.of(2000, 1, 15),
                "000000000",
                "paciente@example.com",
                "Direccion de prueba"
        );
    }
}