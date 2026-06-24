package br.usp.lab.oo.planejador_feriado.recommendation.strategy;

import br.usp.lab.oo.planejador_feriado.recommendation.model.Criterion;
import br.usp.lab.oo.planejador_feriado.recommendation.model.RecommendationContext;
import br.usp.lab.oo.planejador_feriado.recommendation.model.ScoreEntry;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Avalia a proximidade do destino em relação ao Brasil (distância great-circle).
 * Destinos mais perto tendem a significar voos mais baratos e menos tempo perdido
 * em trânsito — relevante para viagens curtas de feriadão.
 */
@Component
public class DistanceStrategy implements ScoringStrategy {

    private static final double MAX_RELEVANT_KM = 18000.0;

    @Override
    public Criterion criterion() {
        return Criterion.DISTANCE;
    }

    @Override
    public ScoreEntry evaluate(RecommendationContext context) {
        Double distanceKm = context.distanceFromOriginKm();
        if (distanceKm == null) {
            return ScoreEntry.unavailable(criterion(), "Distância indisponível para o destino");
        }

        double score = Math.max(5.0, 100.0 * (1.0 - distanceKm / MAX_RELEVANT_KM));
        score = Math.min(100.0, score);

        String justification = String.format(Locale.ROOT,
                "%s: ~%,.0f km da origem", qualitative(score), distanceKm);
        return ScoreEntry.of(criterion(), score, justification);
    }

    private String qualitative(double score) {
        if (score >= 80.0) {
            return "Pertinho";
        }
        if (score >= 55.0) {
            return "Distância média";
        }
        if (score >= 30.0) {
            return "Viagem longa";
        }
        return "Muito distante";
    }
}
