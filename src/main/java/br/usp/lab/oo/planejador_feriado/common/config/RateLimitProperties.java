package br.usp.lab.oo.planejador_feriado.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rate-limit")
public record RateLimitProperties(Integer capacity, Integer refillPerMinute) {

    public int capacityOrDefault() {
        return capacity != null ? capacity : 60;
    }

    public int refillPerMinuteOrDefault() {
        return refillPerMinute != null ? refillPerMinute : 60;
    }
}
