package br.usp.lab.oo.planejador_feriado.recommendation.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.usp.lab.oo.planejador_feriado.cost.model.CostOfLiving;
import br.usp.lab.oo.planejador_feriado.country.model.Country;
import br.usp.lab.oo.planejador_feriado.recommendation.model.Criterion;
import br.usp.lab.oo.planejador_feriado.recommendation.model.RecommendationContext;
import br.usp.lab.oo.planejador_feriado.recommendation.model.RecommendationRequest;
import br.usp.lab.oo.planejador_feriado.recommendation.model.ScoreEntry;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class CostOfLivingStrategyTest {

  private final CostOfLivingStrategy strategy = new CostOfLivingStrategy();
  private final CostOfLiving brazil = new CostOfLiving("BR", 0.6, "2023");

  @Test
  void exposesCostCriterion() {
    assertEquals(Criterion.COST, strategy.criterion());
  }

  @Test
  void scoresHigherWhenCheaperThanBrazil() {
    ScoreEntry cheaper = strategy.evaluate(
      context(new CostOfLiving("AR", 0.36, "2023"))
    );
    ScoreEntry pricier = strategy.evaluate(
      context(new CostOfLiving("CH", 1.2, "2023"))
    );

    assertTrue(cheaper.available());
    assertTrue(cheaper.score() > pricier.score());
    assertTrue(cheaper.justification().contains("% da origem"));
  }

  @Test
  void isUnavailableWhenCostMissing() {
    ScoreEntry entry = strategy.evaluate(context(null));

    assertFalse(entry.available());
    assertTrue(entry.justification().contains("indisponível"));
  }

  private RecommendationContext context(CostOfLiving destinationCost) {
    Country country = new Country(
      "Argentina",
      "AR",
      "Americas",
      "South America",
      List.of("Buenos Aires"),
      List.of("Spanish"),
      List.of("ARS"),
      List.of("UTC-03:00")
    );
    RecommendationRequest request = new RecommendationRequest(
      LocalDate.of(2026, 6, 1),
      LocalDate.of(2026, 6, 30),
      List.of("AR"),
      null,
      10
    );
    return new RecommendationContext(
      country,
      List.of(),
      null,
      null,
      destinationCost,
      brazil,
      null,
      null,
      request
    );
  }
}
