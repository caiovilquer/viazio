package br.usp.lab.oo.planejador_feriado.recommendation.dto;

import br.usp.lab.oo.planejador_feriado.recommendation.model.LongWeekend;
import br.usp.lab.oo.planejador_feriado.recommendation.model.SkippedCandidate;
import br.usp.lab.oo.planejador_feriado.recommendation.model.TravelRecommendation;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Resposta da comparação de destinos. Ecoa o perfil e os pesos efetivamente aplicados
 * (transparência para a UI exibir/ajustar), além do ranking explicável e dos destinos
 * descartados com o motivo.
 *
 * @param profile  nome do perfil resolvido (ex.: "economico", "personalizado")
 * @param weights  pesos aplicados por critério (somam 1)
 */
public record RecommendationResponse(
        LocalDate from,
        LocalDate to,
        String profile,
        Map<String, Double> weights,
        List<LongWeekend> brazilLongWeekends,
        List<TravelRecommendation> recommendations,
        List<SkippedCandidate> skipped
) {
}
