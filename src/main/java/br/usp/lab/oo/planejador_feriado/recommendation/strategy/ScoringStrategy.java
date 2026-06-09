package br.usp.lab.oo.planejador_feriado.recommendation.strategy;

import br.usp.lab.oo.planejador_feriado.recommendation.model.RecommendationContext;
import br.usp.lab.oo.planejador_feriado.recommendation.model.ScoreEntry;

/**
 * Padrão Strategy: cada implementação avalia um critério independente de score.
 * Permite adicionar ou alterar regras sem modificar o motor de recomendação.
 */
public interface ScoringStrategy {

    ScoreEntry evaluate(RecommendationContext context);
}
