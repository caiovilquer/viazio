package br.usp.lab.oo.planejador_feriado.recommendation.strategy;

import br.usp.lab.oo.planejador_feriado.country.model.Country;
import br.usp.lab.oo.planejador_feriado.recommendation.model.Criterion;
import br.usp.lab.oo.planejador_feriado.recommendation.model.RecommendationContext;
import br.usp.lab.oo.planejador_feriado.recommendation.model.RecommendationRequest;
import br.usp.lab.oo.planejador_feriado.recommendation.model.ScoreEntry;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DistanceStrategyTest {

    private final DistanceStrategy strategy = new DistanceStrategy();

    @Test
    void exposesDistanceCriterion() {
        assertEquals(Criterion.DISTANCE, strategy.criterion());
    }

    @Test
    void scoresHigherForCloserDestinations() {
        double near = strategy.evaluate(context(1680.0)).score();
        double far = strategy.evaluate(context(17000.0)).score();

        assertTrue(near > far);
        assertTrue(near >= 80.0, "expected near destination to score high, got " + near);
    }

    @Test
    void isUnavailableWhenDistanceMissing() {
        ScoreEntry entry = strategy.evaluate(context(null));

        assertFalse(entry.available());
        assertTrue(entry.justification().contains("indisponível"));
    }

    private RecommendationContext context(Double distanceKm) {
        Country country = new Country("Argentina", "AR", "Americas", "South America",
                List.of("Buenos Aires"), List.of("Spanish"), List.of("ARS"), List.of("UTC-03:00"));
        RecommendationRequest request = new RecommendationRequest(
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30), List.of("AR"), null, null, 10);
        return new RecommendationContext(
                country, List.of(), List.of(), List.of(), null, null, null, null, distanceKm, request);
    }
}
