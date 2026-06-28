package br.usp.lab.oo.planejador_feriado.recommendation.model;

import java.util.List;

/**
 * Informa quanto do score solicitado foi sustentado por dados disponíveis.
 * Impede que um destino com pouca evidência pareça tão confiável quanto um completo.
 */
public record DataQuality(
  double coverage,
  double confidenceScore,
  int availableCriteria,
  int totalCriteria,
  List<String> missingCriteria
) {}
