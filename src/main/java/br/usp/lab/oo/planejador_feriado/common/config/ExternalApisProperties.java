package br.usp.lab.oo.planejador_feriado.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.external-apis")
public record ExternalApisProperties(
        Api nagerDate,
        Api awesomeApi,
        Api openMeteo,
        Api openMeteoForecast,
        Api worldBank,
        String wikipediaBaseUrlTemplate,
        Duration connectTimeout,
        Duration readTimeout
) {
    @ConstructorBinding
    public ExternalApisProperties {
    }

    public ExternalApisProperties(
            Api nagerDate,
            Api awesomeApi,
            Api openMeteo,
            Api worldBank,
            String wikipediaBaseUrlTemplate,
            Duration connectTimeout,
            Duration readTimeout) {
        this(nagerDate, awesomeApi, openMeteo, openMeteo, worldBank,
                wikipediaBaseUrlTemplate, connectTimeout, readTimeout);
    }

    public record Api(String baseUrl) {
    }

    public Duration connectTimeout() {
        return connectTimeout != null ? connectTimeout : Duration.ofSeconds(3);
    }

    public Duration readTimeout() {
        return readTimeout != null ? readTimeout : Duration.ofSeconds(5);
    }
}
