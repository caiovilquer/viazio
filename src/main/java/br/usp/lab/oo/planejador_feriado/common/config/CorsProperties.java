package br.usp.lab.oo.planejador_feriado.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(List<String> allowedOrigins) {

    public List<String> allowedOrigins() {
        return allowedOrigins != null ? allowedOrigins : List.of();
    }
}
