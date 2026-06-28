package br.usp.lab.oo.planejador_feriado.weather.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Resposta da Open-Meteo Archive API (https://archive-api.open-meteo.com/v1/archive),
 * trazendo séries diárias para o período consultado.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenMeteoArchiveResponse(Daily daily) {
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Daily(
    List<String> time,
    List<Double> temperature_2m_max,
    List<Double> temperature_2m_min,
    List<Double> precipitation_sum
  ) {}
}
