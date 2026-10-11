package pe.edu.cibertec.hospital.gateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator rutas(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("pacientes", ruta -> ruta
                        .path("/api/pacientes", "/api/pacientes/**")
                        .uri("lb://pacientes-service"))
                .route("citas", ruta -> ruta
                        .path("/api/citas", "/api/citas/**")
                        .uri("lb://citas-service"))
                .build();
    }
}