package br.usp.lab.oo.planejador_feriado.destination.model;

import java.util.List;

public record DestinationCity(
  String countryCode,
  String name,
  double latitude,
  double longitude,
  List<Double> utcOffsets,
  boolean primary
) {
  public DestinationCity {
    utcOffsets = utcOffsets != null ? List.copyOf(utcOffsets) : List.of();
  }
}
