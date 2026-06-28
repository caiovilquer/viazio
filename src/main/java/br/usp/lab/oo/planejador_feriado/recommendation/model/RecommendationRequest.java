package br.usp.lab.oo.planejador_feriado.recommendation.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Parâmetros já normalizados de uma busca. O limite controla somente a quantidade
 * retornada; a região inteira é avaliada antes do corte do ranking.
 */
public record RecommendationRequest(
  LocalDate from,
  LocalDate to,
  List<String> countryCodes,
  String region,
  int limit,
  String profile,
  Map<Criterion, Double> weightOverrides,
  List<String> excludedCountryCodes,
  String originCountryCode,
  String originSubdivisionCode,
  Double originLatitude,
  Double originLongitude,
  String originCityName,
  int travelers,
  Double maxGroundBudgetBrl
) {
  public RecommendationRequest {
    countryCodes = countryCodes != null ? List.copyOf(countryCodes) : List.of();
    weightOverrides =
      weightOverrides != null ? Map.copyOf(weightOverrides) : Map.of();
    excludedCountryCodes =
      excludedCountryCodes != null
        ? List.copyOf(excludedCountryCodes)
        : List.of();
    originCountryCode =
      originCountryCode == null || originCountryCode.isBlank()
        ? "BR"
        : originCountryCode;
    travelers = travelers > 0 ? travelers : 1;
  }

  public RecommendationRequest(
    LocalDate from,
    LocalDate to,
    List<String> countryCodes,
    String region,
    int limit
  ) {
    this(
      from,
      to,
      countryCodes,
      region,
      limit,
      null,
      Map.of(),
      List.of(),
      "BR",
      null,
      null,
      null,
      null,
      1,
      null
    );
  }
}
