package br.usp.lab.oo.planejador_feriado.recommendation.strategy;

import br.usp.lab.oo.planejador_feriado.country.model.Country;
import br.usp.lab.oo.planejador_feriado.exchange.model.Exchange;
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

class ExchangeRateStrategyTest {

    private final ExchangeRateStrategy strategy = new ExchangeRateStrategy();

    @Test
    void exposesExchangeCriterion() {
        assertEquals(Criterion.EXCHANGE, strategy.criterion());
    }

    @Test
    void givesTopScoreForVeryFavorableRate() {
        ScoreEntry entry = strategy.evaluate(context(new Exchange("JPY", 0.035)));

        assertTrue(entry.available());
        assertEquals(100.0, entry.score());
        assertTrue(entry.justification().contains("muito favorável"));
    }

    @Test
    void givesLowScoreForExpensiveRate() {
        ScoreEntry entry = strategy.evaluate(context(new Exchange("EUR", 6.30)));

        assertTrue(entry.available());
        assertEquals(25.0, entry.score());
        assertTrue(entry.justification().contains("desfavorável"));
    }

    @Test
    void isUnavailableWhenExchangeMissing() {
        ScoreEntry entry = strategy.evaluate(context(null));

        assertFalse(entry.available());
        assertTrue(entry.justification().contains("indisponível"));
    }

    private RecommendationContext context(Exchange exchange) {
        Country country = new Country("France", "FR", "Europe", "Western Europe",
                List.of("Paris"), List.of("French"), List.of("EUR"), List.of("UTC+01:00"));
        RecommendationRequest request = new RecommendationRequest(
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30), List.of("FR"), null, null, 10);

        return new RecommendationContext(
                country, List.of(), List.of(), List.of(), exchange, null, null, null, null, request);
    }
}
