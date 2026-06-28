package br.usp.lab.oo.planejador_feriado.weather.client;

import br.usp.lab.oo.planejador_feriado.common.config.ExternalApisProperties;
import br.usp.lab.oo.planejador_feriado.common.config.RestClientFactory;
import br.usp.lab.oo.planejador_feriado.weather.dto.OpenMeteoArchiveResponse;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class OpenMeteoClient implements WeatherClient {

  private final RestClient archiveClient;
  private final RestClient forecastClient;

  public OpenMeteoClient(
    RestClientFactory restClientFactory,
    ExternalApisProperties properties
  ) {
    this.archiveClient = restClientFactory
      .builderFor(properties.openMeteo().baseUrl())
      .build();
    this.forecastClient = restClientFactory
      .builderFor(properties.openMeteoForecast().baseUrl())
      .build();
  }

  @Override
  @Retry(name = "weatherApi")
  @CircuitBreaker(name = "weatherApi")
  @Bulkhead(name = "weatherApi")
  public OpenMeteoArchiveResponse getHistoricalDaily(
    double latitude,
    double longitude,
    LocalDate start,
    LocalDate end
  ) {
    return archiveClient
      .get()
      .uri(buildUri("/archive", latitude, longitude, start, end))
      .retrieve()
      .body(OpenMeteoArchiveResponse.class);
  }

  @Override
  @Retry(name = "weatherApi")
  @CircuitBreaker(name = "weatherApi")
  @Bulkhead(name = "weatherApi")
  public OpenMeteoArchiveResponse getForecastDaily(
    double latitude,
    double longitude,
    LocalDate start,
    LocalDate end
  ) {
    return forecastClient
      .get()
      .uri(buildUri("/forecast", latitude, longitude, start, end))
      .retrieve()
      .body(OpenMeteoArchiveResponse.class);
  }

  private String buildUri(
    String path,
    double latitude,
    double longitude,
    LocalDate start,
    LocalDate end
  ) {
    return UriComponentsBuilder.fromPath(path)
      .queryParam("latitude", latitude)
      .queryParam("longitude", longitude)
      .queryParam("start_date", start.format(DateTimeFormatter.ISO_LOCAL_DATE))
      .queryParam("end_date", end.format(DateTimeFormatter.ISO_LOCAL_DATE))
      .queryParam(
        "daily",
        "temperature_2m_max,temperature_2m_min,precipitation_sum"
      )
      .queryParam("timezone", "auto")
      .build()
      .toUriString();
  }
}
