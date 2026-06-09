package br.usp.lab.oo.planejador_feriado.recommendation.strategy;

import br.usp.lab.oo.planejador_feriado.country.model.Country;
import br.usp.lab.oo.planejador_feriado.exchange.model.Exchange;
import br.usp.lab.oo.planejador_feriado.recommendation.model.RecommendationContext;
import br.usp.lab.oo.planejador_feriado.recommendation.model.RecommendationRequest;
import br.usp.lab.oo.planejador_feriado.recommendation.model.ScoreEntry;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExchangeRateStrategyTest {

    private final ExchangeRateStrategy strategy = new ExchangeRateStrategy();

    @Test
    void shouldGiveMaxPointsForVeryFavorableRate() {
        ScoreEntry entry = strategy.evaluate(context(new Exchange("JPY", 0.035), null));

        assertEquals(35.0, entry.points());
        assertTrue(entry.justification().contains("muito favorável"));
    }

    @Test
    void shouldGiveLowPointsForExpensiveRate() {
        ScoreEntry entry = strategy.evaluate(context(new Exchange("EUR", 6.30), null));

        assertEquals(8.0, entry.points());
        assertTrue(entry.justification().contains("desfavorável"));
    }

    @Test
    void shouldReturnZeroWhenAboveMaxRate() {
        ScoreEntry entry = strategy.evaluate(context(new Exchange("EUR", 6.30), 3.0));

        assertEquals(0.0, entry.points());
        assertTrue(entry.justification().contains("orçamento"));
    }

    @Test
    void shouldReturnZeroWhenExchangeUnavailable() {
        ScoreEntry entry = strategy.evaluate(context(null, null));

        assertEquals(0.0, entry.points());
        assertTrue(entry.justification().contains("indisponível"));
    }

    private RecommendationContext context(Exchange exchange, Double maxRate) {
        Country country = new Country("France", "FR", "Europe", "Western Europe",
                List.of("Paris"), List.of("French"), List.of("EUR"), List.of("UTC+01:00"));
        RecommendationRequest request = new RecommendationRequest(
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 30),
                List.of("FR"),
                null,
                maxRate,
                10
        );

        return new RecommendationContext(country, List.of(), List.of(), List.of(), exchange, request);
    }
}
