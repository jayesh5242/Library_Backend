package com.example.Library_backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

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

        // 🔐 Security scheme name for JWT
        final String securitySchemeName = "bearerAuth";

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

                // Available servers (Production + Local)
                .servers(List.of(productionServer, localServer))

                // 🔐 Apply JWT globally to all secured endpoints
                .addSecurityItem(new SecurityRequirement()
                        .addList(securitySchemeName))

                // 🔐 Define JWT Bearer Authentication scheme
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        ));
    }
}