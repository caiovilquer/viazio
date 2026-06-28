package br.usp.lab.oo.planejador_feriado.common.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(List<String> allowedOrigins) {

  public CorsProperties {
    allowedOrigins = normalizeOrigins(allowedOrigins);
  }

  public List<String> allowedOrigins() {
    return allowedOrigins != null ? allowedOrigins : List.of();
  }

  private static List<String> normalizeOrigins(List<String> raw) {
    if (raw == null || raw.isEmpty()) {
      return List.of();
    }
    return raw
      .stream()
      .flatMap(origin -> Arrays.stream(origin.split(",")))
      .map(String::trim)
      .filter(origin -> !origin.isBlank())
      .toList();
  }
}
