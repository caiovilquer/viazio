package br.usp.lab.oo.planejador_feriado.recommendation.strategy;

import br.usp.lab.oo.planejador_feriado.recommendation.model.Criterion;
import br.usp.lab.oo.planejador_feriado.recommendation.model.RecommendationContext;
import br.usp.lab.oo.planejador_feriado.recommendation.model.ScoreEntry;

/**
 * Padrão Strategy: cada implementação avalia um critério independente, produzindo
 * uma nota normalizada de 0 a 100. O motor combina as notas por média ponderada
 * (pesos por perfil/ajuste fino), então adicionar ou alterar uma regra não exige
 * mexer no motor nem nas demais strategies.
 */
public interface ScoringStrategy {
  /** Critério avaliado por esta strategy (define peso, rótulo e ícone). */
  Criterion criterion();

  ScoreEntry evaluate(RecommendationContext context);
}
