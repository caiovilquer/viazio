package br.usp.lab.oo.planejador_feriado.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.external-apis")
public record ExternalApisProperties(
        Api restCountries,
        Api nagerDate,
        Api awesomeApi
) {
    public record Api(String baseUrl) {
    }
}
