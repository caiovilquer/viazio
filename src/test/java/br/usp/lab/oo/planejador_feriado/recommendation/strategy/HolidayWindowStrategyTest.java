package br.usp.lab.oo.planejador_feriado.recommendation.strategy;

import br.usp.lab.oo.planejador_feriado.country.model.Country;
import br.usp.lab.oo.planejador_feriado.holiday.model.Holiday;
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
    void shouldScoreLongWeekendAndDestinationBonus() {
        RecommendationContext context = context(
                List.of(new Holiday(LocalDate.of(2026, 7, 14), "Bastille Day", "Fête", List.of("Public"))),
                List.of(new Holiday(LocalDate.of(2026, 6, 4), "Corpus Christi", "Corpus Christi", List.of("Public"))),
                List.of(new LongWeekend(
                        LocalDate.of(2026, 6, 4),
                        LocalDate.of(2026, 6, 7),
                        4,
                        1,
                        "Corpus Christi"
                ))
        );

        ScoreEntry entry = strategy.evaluate(context);

        assertEquals(30.0, entry.points());
        assertTrue(entry.justification().contains("Feriadão de 4 dias"));
        assertTrue(entry.justification().contains("1 feriado(s) no destino"));
    }

    @Test
    void shouldScoreIsolatedWeekdayHoliday() {
        LocalDate isolated = LocalDate.of(2026, 11, 20);
        RecommendationContext context = context(
                List.of(),
                List.of(new Holiday(isolated, "Consciência Negra", "Consciência Negra", List.of("Public"))),
                List.of()
        );

        ScoreEntry entry = strategy.evaluate(context);

        assertEquals(8.0, entry.points());
    }

    private RecommendationContext context(
            List<Holiday> destinationHolidays,
            List<Holiday> brazilHolidays,
            List<LongWeekend> longWeekends) {

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

        return new RecommendationContext(
                country,
                destinationHolidays,
                brazilHolidays,
                longWeekends,
                null,
                request
        );
    }
}
