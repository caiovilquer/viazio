package br.usp.lab.oo.planejador_feriado.recommendation.service;

import br.usp.lab.oo.planejador_feriado.holiday.model.Holiday;
import br.usp.lab.oo.planejador_feriado.recommendation.detector.LongWeekendDetector;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TravelWindowEvaluatorTest {

    private final TravelWindowEvaluator evaluator =
            new TravelWindowEvaluator(new LongWeekendDetector());

    @Test
    void separatesCalendarQualityFromDestinationScoring() {
        Holiday thursdayHoliday = new Holiday(
                LocalDate.of(2026, 6, 4),
                "Corpus Christi",
                "Corpus Christi",
                List.of("Public"));

        var assessment = evaluator.evaluate(
                List.of(thursdayHoliday),
                LocalDate.of(2026, 6, 4),
                LocalDate.of(2026, 6, 7));

        assertEquals(4, assessment.totalDays());
        assertEquals(3, assessment.freeDays());
        assertEquals(1, assessment.requiredLeaveDays());
        assertFalse(assessment.longWeekends().isEmpty());
        assertTrue(assessment.score() >= 70.0);
    }
}
