package br.usp.lab.oo.planejador_feriado.recommendation.strategy;

import br.usp.lab.oo.planejador_feriado.holiday.model.Holiday;
import br.usp.lab.oo.planejador_feriado.recommendation.model.LongWeekend;
import br.usp.lab.oo.planejador_feriado.recommendation.model.RecommendationContext;
import br.usp.lab.oo.planejador_feriado.recommendation.model.ScoreEntry;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class HolidayWindowStrategy implements ScoringStrategy {

    private static final double MAX_POINTS = 40.0;
    private static final String CRITERION = "FERIADOS_NA_JANELA";

    @Override
    public ScoreEntry evaluate(RecommendationContext context) {
        double brScore = scoreBrazilCalendar(context.brazilLongWeekends(), context.brazilHolidaysInWindow());
        double destinationBonus = Math.min(context.destinationHolidaysInWindow().size() * 5.0, 10.0);
        double points = Math.min(brScore + destinationBonus, MAX_POINTS);

        String justification = buildJustification(
                context.brazilLongWeekends(),
                context.brazilHolidaysInWindow(),
                context.destinationHolidaysInWindow(),
                brScore,
                destinationBonus
        );

        return new ScoreEntry(CRITERION, points, MAX_POINTS, justification);
    }

    private double scoreBrazilCalendar(List<LongWeekend> longWeekends, List<Holiday> brazilHolidays) {
        double score = 0.0;

        for (LongWeekend weekend : longWeekends) {
            if (weekend.totalDays() >= 4) {
                score = Math.max(score, 25.0);
            } else if (weekend.totalDays() == 3) {
                score = Math.max(score, 18.0);
            }
        }

        Set<LocalDate> coveredByLongWeekend = new HashSet<>();
        for (LongWeekend weekend : longWeekends) {
            for (LocalDate date = weekend.start(); !date.isAfter(weekend.end()); date = date.plusDays(1)) {
                coveredByLongWeekend.add(date);
            }
        }

        for (Holiday holiday : brazilHolidays) {
            if (!holiday.isPublicHoliday()) {
                continue;
            }
            LocalDate date = holiday.getDate();
            if (coveredByLongWeekend.contains(date)) {
                continue;
            }
            if (isWeekday(date)) {
                score += 8.0;
            }
        }

        return Math.min(score, 30.0);
    }

    private String buildJustification(
            List<LongWeekend> longWeekends,
            List<Holiday> brazilHolidays,
            List<Holiday> destinationHolidays,
            double brScore,
            double destinationBonus) {

        StringBuilder builder = new StringBuilder();

        if (!longWeekends.isEmpty()) {
            LongWeekend best = longWeekends.stream()
                    .max((a, b) -> Integer.compare(a.totalDays(), b.totalDays()))
                    .orElseThrow();
            builder.append("Feriadão de ")
                    .append(best.totalDays())
                    .append(" dias no BR (")
                    .append(best.holidayName());
            if (best.bridgeDaysUsed() > 0) {
                builder.append(" + ponte");
            }
            builder.append(")");
        } else if (brScore > 0) {
            builder.append("Feriados isolados no calendário BR");
        } else {
            builder.append("Nenhum feriadão relevante no calendário BR");
        }

        if (destinationHolidays.isEmpty()) {
            builder.append("; nenhum feriado no destino");
        } else {
            builder.append("; ")
                    .append(destinationHolidays.size())
                    .append(" feriado(s) no destino");
            if (destinationBonus > 0) {
                builder.append(" (+").append((int) destinationBonus).append(" pts bônus)");
            }
        }

        return builder.toString();
    }

    private boolean isWeekday(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
    }
}
