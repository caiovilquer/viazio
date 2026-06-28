package br.usp.lab.oo.planejador_feriado.recommendation.model;

import java.time.LocalDate;

public record LongWeekend(
  LocalDate start,
  LocalDate end,
  int totalDays,
  int bridgeDaysUsed,
  String holidayName
) {}
