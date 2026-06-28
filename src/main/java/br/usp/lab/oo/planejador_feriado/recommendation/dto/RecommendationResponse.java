package br.usp.lab.oo.planejador_feriado.recommendation.dto;

import br.usp.lab.oo.planejador_feriado.exchange.model.Exchange;
import br.usp.lab.oo.planejador_feriado.recommendation.model.OriginReference;
import br.usp.lab.oo.planejador_feriado.recommendation.model.SkippedCandidate;
import br.usp.lab.oo.planejador_feriado.recommendation.model.TravelRecommendation;
import br.usp.lab.oo.planejador_feriado.recommendation.model.WindowAssessment;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record RecommendationResponse(
        LocalDate from,
        LocalDate to,
        Instant generatedAt,
        OriginReference origin,
        String profile,
        Map<String, Double> weights,
        WindowAssessment window,
        List<TravelRecommendation> recommendations,
        List<SkippedCandidate> skipped,
        Exchange originExchangeToBrl
) {
}
