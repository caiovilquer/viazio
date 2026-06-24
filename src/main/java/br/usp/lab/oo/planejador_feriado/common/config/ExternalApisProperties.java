package br.usp.lab.oo.planejador_feriado.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.external-apis")
public record ExternalApisProperties(
        Api nagerDate,
        Api awesomeApi,
        Api openMeteo,
        Api worldBank,
        Duration connectTimeout,
        Duration readTimeout
) {
    public record Api(String baseUrl) {
    }

    public Duration connectTimeout() {
        return connectTimeout != null ? connectTimeout : Duration.ofSeconds(3);
    }

    public Duration readTimeout() {
        return readTimeout != null ? readTimeout : Duration.ofSeconds(5);
    }
}
