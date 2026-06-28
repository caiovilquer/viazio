package br.usp.lab.oo.planejador_feriado.weather.client;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.usp.lab.oo.planejador_feriado.weather.dto.OpenMeteoArchiveResponse;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CachingWeatherClientTest {

  @Mock
  private OpenMeteoClient delegate;

  private static final OpenMeteoArchiveResponse SAMPLE = new OpenMeteoArchiveResponse(
    new OpenMeteoArchiveResponse.Daily(
      List.of("2026-06-01"),
      List.of(25.0),
      List.of(15.0),
      List.of(0.0)
    )
  );

  @Test
  void cachesRepeatedHistoricalCalls() {
    LocalDate start = LocalDate.of(2026, 6, 1);
    LocalDate end = LocalDate.of(2026, 6, 7);
    when(delegate.getHistoricalDaily(-23.55, -46.63, start, end)).thenReturn(SAMPLE);

    CachingWeatherClient client = new CachingWeatherClient(delegate);

    OpenMeteoArchiveResponse first = client.getHistoricalDaily(
      -23.55,
      -46.63,
      start,
      end
    );
    OpenMeteoArchiveResponse second = client.getHistoricalDaily(
      -23.55,
      -46.63,
      start,
      end
    );

    assertSame(SAMPLE, first);
    assertSame(SAMPLE, second);
    verify(delegate, times(1)).getHistoricalDaily(-23.55, -46.63, start, end);
  }

  @Test
  void doesNotMixHistoricalAndForecastCacheKeys() {
    LocalDate start = LocalDate.of(2026, 6, 1);
    LocalDate end = LocalDate.of(2026, 6, 7);
    when(delegate.getHistoricalDaily(48.85, 2.35, start, end)).thenReturn(SAMPLE);
    when(delegate.getForecastDaily(48.85, 2.35, start, end)).thenReturn(SAMPLE);

    CachingWeatherClient client = new CachingWeatherClient(delegate);
    client.getHistoricalDaily(48.85, 2.35, start, end);
    client.getForecastDaily(48.85, 2.35, start, end);

    verify(delegate, times(1)).getHistoricalDaily(48.85, 2.35, start, end);
    verify(delegate, times(1)).getForecastDaily(48.85, 2.35, start, end);
  }
}
