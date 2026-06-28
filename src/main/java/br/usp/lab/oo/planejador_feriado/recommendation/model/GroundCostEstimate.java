package br.usp.lab.oo.planejador_feriado.recommendation.model;

/**
 * Estimativa de custo terrestre diário, sempre em BRL e ancorada no nível de preços do
 * Brasil (a base em reais representa o custo de um dia no Brasil). É, portanto,
 * independente da origem da viagem: o quanto se gasta por dia num destino é propriedade
 * do destino, não de onde a pessoa parte.
 *
 * @param relativePriceLevel  nível de preços do destino relativo ao Brasil (1.0 = igual)
 * @param destinationDataYear ano dos dados de preços do destino
 * @param referenceDataYear   ano dos dados de preços do Brasil (âncora em BRL)
 */
public record GroundCostEstimate(
        String currency,
        double estimatedDailyPerPerson,
        double estimatedTotal,
        int travelers,
        int days,
        double relativePriceLevel,
        String destinationDataYear,
        String referenceDataYear,
        String confidence,
        String assumption
) {
}
