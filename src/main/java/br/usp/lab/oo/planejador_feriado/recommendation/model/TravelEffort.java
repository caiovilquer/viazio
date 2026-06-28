package br.usp.lab.oo.planejador_feriado.recommendation.model;

public record TravelEffort(
  double distanceKm,
  double estimatedTravelHoursMin,
  double estimatedTravelHoursMax,
  Double originUtcOffset,
  Double destinationUtcOffset,
  Double timeZoneDifferenceHours,
  String classification,
  boolean estimated
) {}
