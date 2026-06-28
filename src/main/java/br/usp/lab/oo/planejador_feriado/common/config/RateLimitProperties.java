package br.usp.lab.oo.planejador_feriado.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties(prefix = "app.rate-limit")
public record RateLimitProperties(
  Integer capacity,
  Integer refillPerMinute,
  Integer maximumClients,
  Boolean trustForwardedHeaders
) {
  @ConstructorBinding
  public RateLimitProperties {
  }

  public RateLimitProperties(Integer capacity, Integer refillPerMinute) {
    this(capacity, refillPerMinute, null, null);
  }

  public int capacityOrDefault() {
    return capacity != null && capacity > 0 ? capacity : 60;
  }

  public int refillPerMinuteOrDefault() {
    return refillPerMinute != null && refillPerMinute > 0
      ? refillPerMinute
      : 60;
  }

  public int maximumClientsOrDefault() {
    return maximumClients != null && maximumClients > 0
      ? maximumClients
      : 10_000;
  }

  public boolean trustForwardedHeadersOrDefault() {
    return Boolean.TRUE.equals(trustForwardedHeaders);
  }
}
