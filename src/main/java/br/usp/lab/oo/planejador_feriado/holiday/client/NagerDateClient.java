package br.usp.lab.oo.planejador_feriado.holiday.client;

import br.usp.lab.oo.planejador_feriado.common.config.ExternalApisProperties;
import br.usp.lab.oo.planejador_feriado.common.config.RestClientFactory;
import br.usp.lab.oo.planejador_feriado.holiday.dto.HolidayDTO;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.List;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class NagerDateClient implements HolidayClient {

  private final RestClient restClient;

  public NagerDateClient(
    RestClientFactory restClientFactory,
    ExternalApisProperties properties
  ) {
    this.restClient = restClientFactory
      .builderFor(properties.nagerDate().baseUrl())
      .build();
  }

  @Override
  @Retry(name = "holidayApi")
  @CircuitBreaker(name = "holidayApi")
  @Bulkhead(name = "holidayApi")
  public List<HolidayDTO> getPublicHolidays(int year, String countryCode) {
    return restClient
      .get()
      .uri("/PublicHolidays/{year}/{countryCode}", year, countryCode)
      .retrieve()
      .body(new ParameterizedTypeReference<List<HolidayDTO>>() {});
  }
}
