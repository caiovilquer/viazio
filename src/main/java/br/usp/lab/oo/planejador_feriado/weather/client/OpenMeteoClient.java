package br.usp.lab.oo.planejador_feriado.weather.client;

import br.usp.lab.oo.planejador_feriado.common.config.ExternalApisProperties;
import br.usp.lab.oo.planejador_feriado.common.config.RestClientFactory;
import br.usp.lab.oo.planejador_feriado.weather.dto.OpenMeteoArchiveResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class OpenMeteoClient implements WeatherClient {

    private final RestClient restClient;

    public OpenMeteoClient(RestClientFactory restClientFactory, ExternalApisProperties properties) {
        this.restClient = restClientFactory.builderFor(properties.openMeteo().baseUrl()).build();
    }

    @Override
    @Retry(name = "externalApi")
    @CircuitBreaker(name = "externalApi")
    public OpenMeteoArchiveResponse getDailyClimate(double latitude, double longitude, LocalDate start, LocalDate end) {
        String uri = UriComponentsBuilder.fromPath("/archive")
                .queryParam("latitude", latitude)
                .queryParam("longitude", longitude)
                .queryParam("start_date", start.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .queryParam("end_date", end.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .queryParam("daily", "temperature_2m_max,temperature_2m_min,precipitation_sum")
                .queryParam("timezone", "auto")
                .build()
                .toUriString();

        return restClient.get()
                .uri(uri)
                .retrieve()
                .body(OpenMeteoArchiveResponse.class);
    }
}
