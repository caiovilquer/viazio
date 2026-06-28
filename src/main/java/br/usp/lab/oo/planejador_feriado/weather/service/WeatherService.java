package br.usp.lab.oo.planejador_feriado.weather.service;

import br.usp.lab.oo.planejador_feriado.weather.client.WeatherClient;
import br.usp.lab.oo.planejador_feriado.weather.dto.OpenMeteoArchiveResponse;
import br.usp.lab.oo.planejador_feriado.weather.model.WeatherSourceType;
import br.usp.lab.oo.planejador_feriado.weather.model.WeatherSummary;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Usa previsão real quando toda a janela está no horizonte de 16 dias da Open-Meteo.
 * Para datas mais distantes, calcula climatologia sobre os dez anos anteriores,
 * comparando exatamente os mesmos dias do calendário em cada ano.
 */
@Service
public class WeatherService {

  private static final int FORECAST_HORIZON_DAYS = 16;
  private static final int CLIMATOLOGY_YEARS = 10;
  private static final double RAINY_DAY_THRESHOLD_MM = 1.0;

  private final WeatherClient client;
  private final Clock clock;

  @Autowired
  public WeatherService(WeatherClient client) {
    this(client, Clock.systemDefaultZone());
  }

  WeatherService(WeatherClient client, Clock clock) {
    this.client = client;
    this.clock = clock;
  }

  public Optional<WeatherSummary> getClimateForWindow(
    double latitude,
    double longitude,
    LocalDate from,
    LocalDate to
  ) {
    LocalDate today = LocalDate.now(clock);
    if (
      !from.isBefore(today) &&
      !to.isAfter(today.plusDays(FORECAST_HORIZON_DAYS))
    ) {
      return summarize(
        client.getForecastDaily(latitude, longitude, from, to),
        WeatherSourceType.FORECAST,
        1,
        from,
        to,
        List.of(new DateWindow(from, to))
      );
    }

    List<DateWindow> samples = new ArrayList<>(CLIMATOLOGY_YEARS);
    for (int yearsBack = CLIMATOLOGY_YEARS; yearsBack >= 1; yearsBack--) {
      samples.add(
        new DateWindow(
          safeMinusYears(from, yearsBack),
          safeMinusYears(to, yearsBack)
        )
      );
    }
    LocalDate historyFrom = samples.get(0).from();
    LocalDate historyTo = samples.get(samples.size() - 1).to();

    return summarize(
      client.getHistoricalDaily(latitude, longitude, historyFrom, historyTo),
      WeatherSourceType.CLIMATOLOGY,
      CLIMATOLOGY_YEARS,
      historyFrom,
      historyTo,
      samples
    );
  }

  private Optional<WeatherSummary> summarize(
    OpenMeteoArchiveResponse response,
    WeatherSourceType sourceType,
    int sampledYears,
    LocalDate referenceFrom,
    LocalDate referenceTo,
    List<DateWindow> includedWindows
  ) {
    if (response == null || response.daily() == null) {
      return Optional.empty();
    }

    OpenMeteoArchiveResponse.Daily daily = response.daily();
    List<String> dates = daily.time();
    List<Double> maxTemps = daily.temperature_2m_max();
    List<Double> minTemps = daily.temperature_2m_min();
    List<Double> precipitation = daily.precipitation_sum();
    if (dates == null || maxTemps == null || minTemps == null) {
      return Optional.empty();
    }

    int size = Math.min(
      dates.size(),
      Math.min(maxTemps.size(), minTemps.size())
    );
    Map<LocalDate, DailySample> byDate = new HashMap<>();
    for (int i = 0; i < size; i++) {
      Double max = maxTemps.get(i);
      Double min = minTemps.get(i);
      if (max == null || min == null) {
        continue;
      }
      Double rain =
        precipitation != null && i < precipitation.size()
          ? precipitation.get(i)
          : null;
      byDate.put(
        LocalDate.parse(dates.get(i)),
        new DailySample((max + min) / 2.0, rain)
      );
    }

    List<DailySample> samples = new ArrayList<>();
    for (DateWindow window : includedWindows) {
      for (
        LocalDate date = window.from();
        !date.isAfter(window.to());
        date = date.plusDays(1)
      ) {
        DailySample sample = byDate.get(date);
        if (sample != null) {
          samples.add(sample);
        }
      }
    }
    if (samples.isEmpty()) {
      return Optional.empty();
    }

    double avgTemp = samples
      .stream()
      .mapToDouble(DailySample::temperature)
      .average()
      .orElseThrow();
    double variance = samples
      .stream()
      .mapToDouble(sample -> Math.pow(sample.temperature() - avgTemp, 2))
      .average()
      .orElse(0.0);
    List<Double> rainValues = samples
      .stream()
      .map(DailySample::precipitation)
      .filter(value -> value != null)
      .toList();
    double avgRain = rainValues
      .stream()
      .mapToDouble(Double::doubleValue)
      .average()
      .orElse(0.0);
    double rainyProbability = rainValues.isEmpty()
      ? 0.0
      : (double) rainValues
          .stream()
          .filter(value -> value >= RAINY_DAY_THRESHOLD_MM)
          .count() / rainValues.size();

    return Optional.of(
      new WeatherSummary(
        avgTemp,
        avgRain,
        rainyProbability,
        Math.sqrt(variance),
        samples.size(),
        sampledYears,
        sourceType,
        referenceFrom,
        referenceTo
      )
    );
  }

  private LocalDate safeMinusYears(LocalDate date, int years) {
    return date.minusYears(years);
  }

  private record DateWindow(LocalDate from, LocalDate to) {}

  private record DailySample(double temperature, Double precipitation) {}
}
