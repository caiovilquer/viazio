package br.usp.lab.oo.planejador_feriado.weather.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.usp.lab.oo.planejador_feriado.weather.client.WeatherClient;
import br.usp.lab.oo.planejador_feriado.weather.dto.OpenMeteoArchiveResponse;
import br.usp.lab.oo.planejador_feriado.weather.model.WeatherSourceType;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class WeatherServiceTest {

  private final WeatherClient client = mock(WeatherClient.class);
  private final Clock clock = Clock.fixed(
    Instant.parse("2026-06-23T12:00:00Z"),
    ZoneOffset.UTC
  );
  private final WeatherService service = new WeatherService(client, clock);

  @Test
  void usesForecastForWindowInsideSixteenDayHorizon() {
    LocalDate from = LocalDate.of(2026, 6, 24);
    LocalDate to = LocalDate.of(2026, 6, 26);
    when(client.getForecastDaily(-23.5, -46.6, from, to)).thenReturn(
      response(
        List.of("2026-06-24", "2026-06-25", "2026-06-26"),
        List.of(26.0, 25.0, 24.0),
        List.of(16.0, 15.0, 14.0),
        List.of(0.0, 2.0, 0.0)
      )
    );

    var summary = service
      .getClimateForWindow(-23.5, -46.6, from, to)
      .orElseThrow();

    assertEquals(WeatherSourceType.FORECAST, summary.sourceType());
    assertEquals(3, summary.sampledDays());
    assertEquals(1.0 / 3.0, summary.rainyDayProbability(), 0.0001);
    verify(client).getForecastDaily(-23.5, -46.6, from, to);
  }

  @Test
  void computesTenYearClimatologyForDistantWindow() {
    LocalDate from = LocalDate.of(2027, 7, 1);
    LocalDate to = LocalDate.of(2027, 7, 2);
    List<String> dates = new ArrayList<>();
    List<Double> max = new ArrayList<>();
    List<Double> min = new ArrayList<>();
    List<Double> rain = new ArrayList<>();
    for (int year = 2017; year <= 2026; year++) {
      dates.add(year + "-07-01");
      dates.add(year + "-07-02");
      max.add(26.0);
      max.add(28.0);
      min.add(16.0);
      min.add(18.0);
      rain.add(0.0);
      rain.add(2.0);
    }
    when(
      client.getHistoricalDaily(
        -23.5,
        -46.6,
        LocalDate.of(2017, 7, 1),
        LocalDate.of(2026, 7, 2)
      )
    ).thenReturn(response(dates, max, min, rain));

    var summary = service
      .getClimateForWindow(-23.5, -46.6, from, to)
      .orElseThrow();

    assertEquals(WeatherSourceType.CLIMATOLOGY, summary.sourceType());
    assertEquals(10, summary.sampledYears());
    assertEquals(20, summary.sampledDays());
    assertEquals(22.0, summary.avgTempC(), 0.0001);
    assertEquals(0.5, summary.rainyDayProbability(), 0.0001);
    assertTrue(summary.tempStdDevC() > 0.0);
  }

  private OpenMeteoArchiveResponse response(
    List<String> dates,
    List<Double> max,
    List<Double> min,
    List<Double> rain
  ) {
    return new OpenMeteoArchiveResponse(
      new OpenMeteoArchiveResponse.Daily(dates, max, min, rain)
    );
  }
}
