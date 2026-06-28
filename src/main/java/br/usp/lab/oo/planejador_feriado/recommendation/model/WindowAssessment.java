package br.usp.lab.oo.planejador_feriado.recommendation.model;

import java.util.List;

/** Qualidade da janela no calendário de origem, independente do destino. */
public record WindowAssessment(
  double score,
  int totalDays,
  int freeDays,
  int requiredLeaveDays,
  List<LongWeekend> longWeekends,
  String explanation
) {}
