package br.usp.lab.oo.planejador_feriado.recommendation.model;

public record OriginReference(
  String countryCode,
  String subdivisionCode,
  double latitude,
  double longitude,
  String cityName
) {
  public OriginReference(
    String countryCode,
    String subdivisionCode,
    double latitude,
    double longitude
  ) {
    this(countryCode, subdivisionCode, latitude, longitude, null);
  }
}
