package br.usp.lab.oo.planejador_feriado.meta.dto;

public record CityOption(
  String name,
  double latitude,
  double longitude,
  boolean primary
) {}
