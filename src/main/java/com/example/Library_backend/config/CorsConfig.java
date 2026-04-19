package com.example.Library_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class CorsConfig {

        @Value("${app.frontend-url}")
        private String frontendUrl;

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {

                CorsConfiguration config = new CorsConfiguration();

                // Parse and trim the comma-separated or single URL
                List<String> allowedOrigins = Arrays.stream(frontendUrl.split(","))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .collect(Collectors.toList());
                config.setAllowedOrigins(allowedOrigins);

                config.setAllowedMethods(List.of(
                                "GET", "POST", "PUT",
                                "DELETE", "PATCH", "OPTIONS"));

                config.setAllowedHeaders(List.of("*"));
                config.setExposedHeaders(List.of("Authorization", "Content-Type"));
                config.setAllowCredentials(true);
                config.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", config);

                return source;
        }
}