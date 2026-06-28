package br.usp.lab.oo.planejador_feriado.recommendation.model;

import br.usp.lab.oo.planejador_feriado.enrichment.model.DestinationProfile;
import br.usp.lab.oo.planejador_feriado.exchange.model.Exchange;
import br.usp.lab.oo.planejador_feriado.weather.model.WeatherSummary;

import java.util.List;

/**
 * Ranking explicável. destinationScore mede adequação do destino; windowScore mede
 * o calendário de origem; tripScore combina ambos e aplica a confiança dos dados.
 * O câmbio é informativo e nunca participa do score. O mesmo vale para climate:
 * é o resumo climático já usado pela WeatherStrategy, exposto para a UI plotar um
 * gráfico, independente do peso dado ao critério de clima.
 */
public record TravelRecommendation(
        String countryCode,
        String countryName,
        double destinationScore,
        double windowScore,
        double tripScore,
        DataQuality dataQuality,
        List<ScoredCriterion> breakdown,
        List<String> highlights,
        List<String> tradeoffs,
        String summary,
        Exchange exchangeToBrl,
        DestinationProfile profile,
        TripFeasibility feasibility,
        WeatherSummary climate
) {
    public TravelRecommendation(
            String countryCode,
            String countryName,
            double destinationScore,
            double windowScore,
            double tripScore,
            DataQuality dataQuality,
            List<ScoredCriterion> breakdown,
            List<String> highlights,
            List<String> tradeoffs,
            String summary,
            Exchange exchangeToBrl,
            DestinationProfile profile) {
        this(countryCode, countryName, destinationScore, windowScore, tripScore, dataQuality,
                breakdown, highlights, tradeoffs, summary, exchangeToBrl, profile, null, null);
    }

    public TravelRecommendation(
            String countryCode,
            String countryName,
            double destinationScore,
            double windowScore,
            double tripScore,
            DataQuality dataQuality,
            List<ScoredCriterion> breakdown,
            List<String> highlights,
            List<String> tradeoffs,
            String summary,
            Exchange exchangeToBrl,
            DestinationProfile profile,
            TripFeasibility feasibility) {
        this(countryCode, countryName, destinationScore, windowScore, tripScore, dataQuality,
                breakdown, highlights, tradeoffs, summary, exchangeToBrl, profile, feasibility, null);
    }
}
