package br.usp.lab.oo.planejador_feriado.recommendation.strategy;

import br.usp.lab.oo.planejador_feriado.country.model.Country;
import br.usp.lab.oo.planejador_feriado.holiday.model.Holiday;
import br.usp.lab.oo.planejador_feriado.recommendation.model.RecommendationContext;
import br.usp.lab.oo.planejador_feriado.recommendation.model.RecommendationRequest;
import br.usp.lab.oo.planejador_feriado.recommendation.model.ScoreEntry;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FreeDaysRatioStrategyTest {

    private final FreeDaysRatioStrategy strategy = new FreeDaysRatioStrategy();

    @Test
    void shouldCalculateFreeDaysRatioForJune2026() {
        Holiday corpusChristi = new Holiday(
                LocalDate.of(2026, 6, 4),
                "Corpus Christi",
                "Corpus Christi",
                List.of("Public")
        );

        Country country = new Country("Japan", "JP", "Asia", "Eastern Asia",
                List.of("Tokyo"), List.of("Japanese"), List.of("JPY"), List.of("UTC+09:00"));
        RecommendationRequest request = new RecommendationRequest(
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 30),
                List.of("JP"),
                null,
                null,
                10
        );
        RecommendationContext context = new RecommendationContext(
                country,
                List.of(),
                List.of(corpusChristi),
                List.of(),
                null,
                request
        );

        ScoreEntry entry = strategy.evaluate(context);

        assertEquals(8.0, entry.points());
        assertEquals("9 dias livres em 30 na janela", entry.justification());
    }
}
