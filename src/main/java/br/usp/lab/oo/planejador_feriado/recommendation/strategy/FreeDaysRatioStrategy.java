package br.usp.lab.oo.planejador_feriado.recommendation.strategy;

import br.usp.lab.oo.planejador_feriado.holiday.model.Holiday;
import br.usp.lab.oo.planejador_feriado.recommendation.model.RecommendationContext;
import br.usp.lab.oo.planejador_feriado.recommendation.model.ScoreEntry;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Component
public class FreeDaysRatioStrategy implements ScoringStrategy {

    private static final double MAX_POINTS = 25.0;
    private static final String CRITERION = "DIAS_LIVRES";

    @Override
    public ScoreEntry evaluate(RecommendationContext context) {
        LocalDate from = context.request().from();
        LocalDate to = context.request().to();

        int totalDays = (int) (to.toEpochDay() - from.toEpochDay()) + 1;
        int freeDays = countFreeDays(from, to, context.brazilHolidaysInWindow());
        double points = Math.round(MAX_POINTS * ((double) freeDays / totalDays));

        String justification = String.format("%d dias livres em %d na janela", freeDays, totalDays);
        return new ScoreEntry(CRITERION, points, MAX_POINTS, justification);
    }

    private int countFreeDays(LocalDate from, LocalDate to, java.util.List<Holiday> brazilHolidays) {
        Set<LocalDate> publicHolidayDates = new HashSet<>();
        for (Holiday holiday : brazilHolidays) {
            if (holiday.isPublicHoliday()) {
                publicHolidayDates.add(holiday.getDate());
            }
        }

        int freeDays = 0;
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            if (isWeekend(date) || publicHolidayDates.contains(date)) {
                freeDays++;
            }
        }
        return freeDays;
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }
}
