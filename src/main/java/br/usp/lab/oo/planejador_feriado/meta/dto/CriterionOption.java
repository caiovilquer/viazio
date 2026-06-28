package br.usp.lab.oo.planejador_feriado.meta.dto;

public record CriterionOption(
  String key,
  String label,
  String icon,
  double defaultWeight
) {}
