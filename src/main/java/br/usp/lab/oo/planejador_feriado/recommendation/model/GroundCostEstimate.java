package br.usp.lab.oo.planejador_feriado.recommendation.model;

public record GroundCostEstimate(
        String currency,
        double estimatedDailyPerPerson,
        double estimatedTotal,
        int travelers,
        int days,
        double relativePriceLevel,
        String destinationDataYear,
        String originDataYear,
        String confidence,
        String assumption
) {
}
