package br.usp.lab.oo.planejador_feriado.recommendation.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.usp.lab.oo.planejador_feriado.country.model.Country;
import br.usp.lab.oo.planejador_feriado.holiday.model.Holiday;
import br.usp.lab.oo.planejador_feriado.recommendation.model.Criterion;
import br.usp.lab.oo.planejador_feriado.recommendation.model.RecommendationContext;
import br.usp.lab.oo.planejador_feriado.recommendation.model.RecommendationRequest;
import br.usp.lab.oo.planejador_feriado.recommendation.model.ScoreEntry;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class DestinationFestivitiesStrategyTest {

  private final DestinationFestivitiesStrategy strategy =
    new DestinationFestivitiesStrategy();

  @Test
  void exposesFestivitiesCriterion() {
    assertEquals(Criterion.FESTIVITIES, strategy.criterion());
  }

  @Test
  void scoresHigherWithMoreLocalHolidays() {
    ScoreEntry withFestivities = strategy.evaluate(
      context(
        List.of(
          new Holiday(
            LocalDate.of(2026, 6, 10),
            "Festa A",
            "Festa A",
            List.of("Public")
          ),
          new Holiday(
            LocalDate.of(2026, 6, 20),
            "Festa B",
            "Festa B",
            List.of("Public")
          )
        )
      )
    );
    ScoreEntry none = strategy.evaluate(context(List.of()));

    assertTrue(withFestivities.score() > none.score());
    assertTrue(
      withFestivities.justification().contains("feriado(s) nacionais")
    );
    assertTrue(none.justification().contains("Sem feriados nacionais"));
  }

  private RecommendationContext context(List<Holiday> destinationHolidays) {
    Country country = new Country(
      "Japan",
      "JP",
      "Asia",
      "Eastern Asia",
      List.of("Tokyo"),
      List.of("Japanese"),
      List.of("JPY"),
      List.of("UTC+09:00")
    );
    RecommendationRequest request = new RecommendationRequest(
      LocalDate.of(2026, 6, 1),
      LocalDate.of(2026, 6, 30),
      List.of("JP"),
      null,
      10
    );
    return new RecommendationContext(
      country,
      destinationHolidays,
      null,
      null,
      null,
      null,
      null,
      null,
      request
    );
  }
}
