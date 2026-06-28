package br.usp.lab.oo.planejador_feriado.recommendation.strategy;

import br.usp.lab.oo.planejador_feriado.cost.model.CostOfLiving;
import br.usp.lab.oo.planejador_feriado.recommendation.model.Criterion;
import br.usp.lab.oo.planejador_feriado.recommendation.model.RecommendationContext;
import br.usp.lab.oo.planejador_feriado.recommendation.model.ScoreEntry;
import java.util.Locale;
import org.springframework.stereotype.Component;

/**
 * Avalia o custo de vida real do destino comparado à origem, usando o nível de
 * preços PPP do Banco Mundial. Corrige o engano do câmbio nominal: mede se as coisas
 * são de fato mais baratas ou mais caras que na origem. Destino com custo igual ao
 * da origem pontua ~50; metade do custo ~75; o dobro, perto de 0.
 */
@Component
public class CostOfLivingStrategy implements ScoringStrategy {

  @Override
  public Criterion criterion() {
    return Criterion.COST;
  }

  @Override
  public ScoreEntry evaluate(RecommendationContext context) {
    CostOfLiving destination = context.destinationCost();
    CostOfLiving origin = context.originCost();

    if (
      destination == null || origin == null || origin.priceLevelRatio() <= 0
    ) {
      return ScoreEntry.unavailable(
        criterion(),
        "Custo de vida indisponível para o destino"
      );
    }

    double relative = destination.priceLevelRatio() / origin.priceLevelRatio();
    double score = Math.max(0.0, Math.min(100.0, 100.0 - relative * 50.0));

    String justification = String.format(
      Locale.ROOT,
      "%s: nível de preços ~%.0f%% da origem (dados %s/%s)",
      qualitative(relative),
      relative * 100.0,
      destination.year(),
      origin.year()
    );
    return ScoreEntry.of(criterion(), score, justification);
  }

  private String qualitative(double relative) {
    if (relative <= 0.7) {
      return "Bem mais barato";
    }
    if (relative <= 0.95) {
      return "Mais barato";
    }
    if (relative <= 1.1) {
      return "Custo parecido";
    }
    if (relative <= 1.5) {
      return "Mais caro";
    }
    return "Bem mais caro";
  }
}
