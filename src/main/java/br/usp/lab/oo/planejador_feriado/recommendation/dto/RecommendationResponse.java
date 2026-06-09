package br.usp.lab.oo.planejador_feriado.recommendation.dto;

import br.usp.lab.oo.planejador_feriado.recommendation.model.LongWeekend;
import br.usp.lab.oo.planejador_feriado.recommendation.model.SkippedCandidate;
import br.usp.lab.oo.planejador_feriado.recommendation.model.TravelRecommendation;

import java.time.LocalDate;
import java.util.List;

public record RecommendationResponse(
        LocalDate from,
        LocalDate to,
        List<LongWeekend> brazilLongWeekends,
        List<TravelRecommendation> recommendations,
        List<SkippedCandidate> skipped
) {
}
