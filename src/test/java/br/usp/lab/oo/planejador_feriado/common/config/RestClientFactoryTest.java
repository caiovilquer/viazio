package br.usp.lab.oo.planejador_feriado.common.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class RestClientFactoryTest {

    @Test
    void buildsClientWithConfiguredTimeoutsWithoutFailing() {
        ExternalApisProperties properties = new ExternalApisProperties(
                new ExternalApisProperties.Api("https://example.com"),
                new ExternalApisProperties.Api("https://example.com"),
                new ExternalApisProperties.Api("https://example.com"),
                new ExternalApisProperties.Api("https://example.com"),
                "https://%s.example.com",
                Duration.ofSeconds(1),
                Duration.ofSeconds(2));

        RestClientFactory factory = new RestClientFactory(properties);
        RestClient client = factory.builderFor("https://example.com").build();

        assertNotNull(client);
    }

    @Test
    void fallsBackToDefaultTimeoutsWhenNotConfigured() {
        ExternalApisProperties properties = new ExternalApisProperties(
                new ExternalApisProperties.Api("https://example.com"),
                new ExternalApisProperties.Api("https://example.com"),
                new ExternalApisProperties.Api("https://example.com"),
                new ExternalApisProperties.Api("https://example.com"),
                "https://%s.example.com",
                null,
                null);

        RestClientFactory factory = new RestClientFactory(properties);

        assertNotNull(factory.builderFor("https://example.com").build());
    }
}
