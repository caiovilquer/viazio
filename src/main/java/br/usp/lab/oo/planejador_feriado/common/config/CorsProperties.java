package br.usp.lab.oo.planejador_feriado.common.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(List<String> allowedOrigins) {
  public List<String> allowedOrigins() {
    return allowedOrigins != null ? allowedOrigins : List.of();
  }
}
