package br.usp.lab.oo.planejador_feriado.weather.client;

import br.usp.lab.oo.planejador_feriado.weather.dto.OpenMeteoArchiveResponse;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Locale;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Decorator (GoF) que adiciona cache em memória sobre {@link OpenMeteoClient}.
 * Dados climáticos históricos não mudam, então o TTL é longo; isso evita repetir
 * a consulta para o mesmo destino/janela ao comparar destinos ou janelas.
 */
@Primary
@Component
public class CachingWeatherClient implements WeatherClient {

  private final WeatherClient delegate;
  private final Cache<String, OpenMeteoArchiveResponse> cache;

  public CachingWeatherClient(OpenMeteoClient delegate) {
    this.delegate = delegate;
    this.cache = Caffeine.newBuilder()
      .maximumSize(1000)
      .expireAfterWrite(Duration.ofDays(7))
      .build();
  }

  @Override
  public OpenMeteoArchiveResponse getHistoricalDaily(
    double latitude,
    double longitude,
    LocalDate start,
    LocalDate end
  ) {
    String key = key("history", latitude, longitude, start, end);
    return cache.get(key, k ->
      delegate.getHistoricalDaily(latitude, longitude, start, end)
    );
  }

  @Override
  public OpenMeteoArchiveResponse getForecastDaily(
    double latitude,
    double longitude,
    LocalDate start,
    LocalDate end
  ) {
    String key = key("forecast", latitude, longitude, start, end);
    return cache.get(key, k ->
      delegate.getForecastDaily(latitude, longitude, start, end)
    );
  }

  private String key(
    String source,
    double latitude,
    double longitude,
    LocalDate start,
    LocalDate end
  ) {
    return String.format(
      Locale.ROOT,
      "%s:%.2f:%.2f:%s:%s",
      source,
      latitude,
      longitude,
      start,
      end
    );
  }
}
