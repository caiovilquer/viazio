package br.usp.lab.oo.planejador_feriado.recommendation.strategy;

import br.usp.lab.oo.planejador_feriado.holiday.model.Holiday;
import br.usp.lab.oo.planejador_feriado.recommendation.model.Criterion;
import br.usp.lab.oo.planejador_feriado.recommendation.model.LongWeekend;
import br.usp.lab.oo.planejador_feriado.recommendation.model.RecommendationContext;
import br.usp.lab.oo.planejador_feriado.recommendation.model.ScoreEntry;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Avalia o quanto a janela é favorável para viajar segundo o calendário brasileiro:
 * qualidade do melhor feriadão/ponte combinada com a proporção de dias livres
 * (fins de semana + feriados públicos). É o mesmo para todos os destinos numa
 * comparação — mas é o critério decisivo ao comparar diferentes janelas.
 */
@Component
public class HolidayWindowStrategy implements ScoringStrategy {

    @Override
    public Criterion criterion() {
        return Criterion.HOLIDAYS;
    }

    @Override
    public ScoreEntry evaluate(RecommendationContext context) {
        List<LongWeekend> longWeekends = context.brazilLongWeekends();
        List<Holiday> brazilHolidays = context.brazilHolidaysInWindow();
        LocalDate from = context.request().from();
        LocalDate to = context.request().to();

        double longWeekendComponent = longWeekendComponent(longWeekends, brazilHolidays);

        int totalDays = (int) (to.toEpochDay() - from.toEpochDay()) + 1;
        int freeDays = countFreeDays(from, to, brazilHolidays);
        double freeDaysComponent = totalDays > 0 ? (double) freeDays / totalDays * 100.0 : 0.0;

        double score = 0.7 * longWeekendComponent + 0.3 * freeDaysComponent;

        return ScoreEntry.of(criterion(), score, buildJustification(longWeekends, freeDays, totalDays));
    }

    private double longWeekendComponent(List<LongWeekend> longWeekends, List<Holiday> brazilHolidays) {
        int bestDays = longWeekends.stream().mapToInt(LongWeekend::totalDays).max().orElse(0);
        if (bestDays >= 6) {
            return 100.0;
        }
        if (bestDays == 5) {
            return 92.0;
        }
        if (bestDays == 4) {
            return 80.0;
        }
        if (bestDays == 3) {
            return 60.0;
        }
        boolean hasIsolatedHoliday = brazilHolidays.stream().anyMatch(Holiday::isPublicHoliday);
        return hasIsolatedHoliday ? 35.0 : 0.0;
    }

    private int countFreeDays(LocalDate from, LocalDate to, List<Holiday> brazilHolidays) {
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

    private String buildJustification(List<LongWeekend> longWeekends, int freeDays, int totalDays) {
        StringBuilder builder = new StringBuilder();
        if (!longWeekends.isEmpty()) {
            LongWeekend best = longWeekends.stream()
                    .max((a, b) -> Integer.compare(a.totalDays(), b.totalDays()))
                    .orElseThrow();
            builder.append("Feriadão de ").append(best.totalDays()).append(" dias (").append(best.holidayName());
            if (best.bridgeDaysUsed() > 0) {
                builder.append(" + ponte");
            }
            builder.append(")");
        } else {
            builder.append("Sem feriadão relevante no calendário BR");
        }
        builder.append("; ").append(freeDays).append(" de ").append(totalDays).append(" dias livres na janela");
        return builder.toString();
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }
}
