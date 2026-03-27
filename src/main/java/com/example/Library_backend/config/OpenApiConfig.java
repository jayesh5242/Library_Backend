package com.example.Library_backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Bean
    public OpenAPI customOpenAPI() {

        // Local development server
        Server localServer = new Server();
        localServer.setUrl("http://localhost:9090");
        localServer.setDescription("Local Development");

        // Railway production server — HTTPS is required!
        Server productionServer = new Server();
        productionServer.setUrl(
                "https://librarybackend-production-67b6.up.railway.app");
        productionServer.setDescription("Production (Railway)");

        // Project info shown in Swagger UI header
        Info info = new Info()
                .title("Multi-Branch College Library Network")
                .version("1.0.0")
                .description("Complete REST API for college library management. "
                        + "41 APIs across 8 modules. JWT secured.")
                .contact(new Contact()
                        .name("Library Team")
                        .email("jayesh.adriit@gmail.com"));

        return new OpenAPI()
                .info(info)
                .servers(List.of(productionServer, localServer));
    }
}