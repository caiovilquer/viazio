package br.usp.lab.oo.planejador_feriado.recommendation.strategy;

import br.usp.lab.oo.planejador_feriado.country.model.Country;
import br.usp.lab.oo.planejador_feriado.holiday.model.Holiday;
import br.usp.lab.oo.planejador_feriado.recommendation.model.Criterion;
import br.usp.lab.oo.planejador_feriado.recommendation.model.LongWeekend;
import br.usp.lab.oo.planejador_feriado.recommendation.model.RecommendationContext;
import br.usp.lab.oo.planejador_feriado.recommendation.model.RecommendationRequest;
import br.usp.lab.oo.planejador_feriado.recommendation.model.ScoreEntry;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HolidayWindowStrategyTest {

    private final HolidayWindowStrategy strategy = new HolidayWindowStrategy();

    @Test
    void exposesHolidaysCriterion() {
        assertEquals(Criterion.HOLIDAYS, strategy.criterion());
    }

    @Test
    void scoresHighWhenWindowHasFourDayLongWeekend() {
        RecommendationContext context = context(
                List.of(new Holiday(LocalDate.of(2026, 6, 4), "Corpus Christi", "Corpus Christi", List.of("Public"))),
                List.of(new LongWeekend(
                        LocalDate.of(2026, 6, 4), LocalDate.of(2026, 6, 7), 4, 1, "Corpus Christi")));

        ScoreEntry entry = strategy.evaluate(context);

        assertTrue(entry.available());
        assertTrue(entry.score() > 55.0, "expected a strong holiday score, got " + entry.score());
        assertTrue(entry.justification().contains("Feriadão de 4 dias"));
    }

    @Test
    void scoresLowWhenWindowHasNoHolidays() {
        ScoreEntry entry = strategy.evaluate(context(List.of(), List.of()));

        assertTrue(entry.available());
        assertTrue(entry.score() < 35.0, "expected a weak holiday score, got " + entry.score());
        assertTrue(entry.justification().contains("Sem feriadão"));
    }

    private RecommendationContext context(List<Holiday> brazilHolidays, List<LongWeekend> longWeekends) {
        Country country = new Country("Japan", "JP", "Asia", "Eastern Asia",
                List.of("Tokyo"), List.of("Japanese"), List.of("JPY"), List.of("UTC+09:00"));
        RecommendationRequest request = new RecommendationRequest(
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30), List.of("JP"), null, null, 10);

        return new RecommendationContext(
                country, List.of(), brazilHolidays, longWeekends, null, null, null, null, null, request);
    }
}
