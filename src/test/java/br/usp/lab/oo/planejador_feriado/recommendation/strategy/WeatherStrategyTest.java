package br.usp.lab.oo.planejador_feriado.recommendation.strategy;

import br.usp.lab.oo.planejador_feriado.country.model.Country;
import br.usp.lab.oo.planejador_feriado.recommendation.model.Criterion;
import br.usp.lab.oo.planejador_feriado.recommendation.model.RecommendationContext;
import br.usp.lab.oo.planejador_feriado.recommendation.model.RecommendationRequest;
import br.usp.lab.oo.planejador_feriado.recommendation.model.ScoreEntry;
import br.usp.lab.oo.planejador_feriado.weather.model.WeatherSummary;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeatherStrategyTest {

    private final WeatherStrategy strategy = new WeatherStrategy();

    @Test
    void exposesWeatherCriterion() {
        assertEquals(Criterion.WEATHER, strategy.criterion());
    }

    @Test
    void scoresHighForMildDryWeather() {
        ScoreEntry entry = strategy.evaluate(context(new WeatherSummary(24.0, 0.5, 10)));

        assertTrue(entry.available());
        assertTrue(entry.score() >= 80.0, "expected great weather score, got " + entry.score());
        assertTrue(entry.justification().contains("Clima"));
    }

    @Test
    void scoresLowForHotRainyWeather() {
        ScoreEntry entry = strategy.evaluate(context(new WeatherSummary(40.0, 10.0, 10)));

        assertTrue(entry.available());
        assertTrue(entry.score() < 40.0, "expected poor weather score, got " + entry.score());
    }

    @Test
    void isUnavailableWhenWeatherMissing() {
        ScoreEntry entry = strategy.evaluate(context(null));

        assertFalse(entry.available());
        assertTrue(entry.justification().contains("indisponível"));
    }

    private RecommendationContext context(WeatherSummary weather) {
        Country country = new Country("Japan", "JP", "Asia", "Eastern Asia",
                List.of("Tokyo"), List.of("Japanese"), List.of("JPY"), List.of("UTC+09:00"));
        RecommendationRequest request = new RecommendationRequest(
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30), List.of("JP"), null, 10);
        return new RecommendationContext(
                country, List.of(), null, weather, null, null, null, null, request);
    }
}
