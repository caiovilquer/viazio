package br.usp.lab.oo.planejador_feriado.recommendation.service;

import br.usp.lab.oo.planejador_feriado.holiday.model.Holiday;
import br.usp.lab.oo.planejador_feriado.recommendation.detector.LongWeekendDetector;
import br.usp.lab.oo.planejador_feriado.recommendation.model.LongWeekend;
import br.usp.lab.oo.planejador_feriado.recommendation.model.WindowAssessment;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
public class TravelWindowEvaluator {

    private final LongWeekendDetector detector;

    public TravelWindowEvaluator(LongWeekendDetector detector) {
        this.detector = detector;
    }

    public WindowAssessment evaluate(List<Holiday> originHolidays, LocalDate from, LocalDate to) {
        List<LongWeekend> longWeekends = detector.detect(originHolidays, from, to);
        Set<LocalDate> holidayDates = new HashSet<>();
        originHolidays.stream()
                .filter(Holiday::isPublicHoliday)
                .map(Holiday::getDate)
                .forEach(holidayDates::add);

        int totalDays = 0;
        int freeDays = 0;
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            totalDays++;
            if (isWeekend(date) || holidayDates.contains(date)) {
                freeDays++;
            }
        }
        int requiredLeaveDays = Math.max(0, totalDays - freeDays);
        int bestWeekend = longWeekends.stream().mapToInt(LongWeekend::totalDays).max().orElse(0);
        double freeRatio = totalDays == 0 ? 0.0 : (double) freeDays / totalDays;
        double score = Math.min(100.0,
                freeRatio * 60.0 + Math.min(1.0, bestWeekend / 6.0) * 40.0);
        score = Math.round(score * 10.0) / 10.0;

        String explanation = String.format(Locale.ROOT,
                "%d de %d dias livres; requer %d dia(s) útil(eis) de folga%s",
                freeDays,
                totalDays,
                requiredLeaveDays,
                bestWeekend > 0 ? "; melhor feriadão de " + bestWeekend + " dias" : "");
        return new WindowAssessment(
                score,
                totalDays,
                freeDays,
                requiredLeaveDays,
                longWeekends,
                explanation);
    }

    private boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY
                || date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }
}
