package br.usp.lab.oo.planejador_feriado.recommendation.detector;

import br.usp.lab.oo.planejador_feriado.holiday.model.Holiday;
import br.usp.lab.oo.planejador_feriado.recommendation.model.LongWeekend;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Detecta feriadões e pontes no calendário da origem dentro de uma janela de viagem.
 * Considera feriados públicos encostados em fins de semana ou com ponte de 1 dia útil.
 */
public final class LongWeekendDetector {

    public List<LongWeekend> detect(List<Holiday> holidays, LocalDate from, LocalDate to) {
        List<Holiday> publicHolidays = holidays.stream()
                .filter(Holiday::isPublicHoliday)
                .filter(h -> !h.getDate().isBefore(from) && !h.getDate().isAfter(to))
                .sorted(Comparator.comparing(Holiday::getDate))
                .toList();

        if (publicHolidays.isEmpty()) {
            return List.of();
        }

        Set<LocalDate> holidayDates = publicHolidays.stream()
                .map(Holiday::getDate)
                .collect(Collectors.toSet());

        List<LongWeekend> result = new ArrayList<>();
        Set<LocalDate> covered = new HashSet<>();

        for (Holiday holiday : publicHolidays) {
            if (covered.contains(holiday.getDate())) {
                continue;
            }

            LocalDate start = holiday.getDate();
            LocalDate end = holiday.getDate();
            int bridgeDays = 0;

            start = expandWeekendsBackward(start, from);
            LocalDate dayBefore = start.minusDays(1);
            if (canBridgeBefore(dayBefore, from, holidayDates)) {
                start = dayBefore;
                bridgeDays++;
                start = expandWeekendsBackward(start, from);
            }

            end = expandWeekendsForward(end, to);
            LocalDate dayAfter = end.plusDays(1);
            if (canBridgeAfter(dayAfter, to, holidayDates)) {
                end = dayAfter;
                bridgeDays++;
                end = expandWeekendsForward(end, to);
            }

            int totalDays = (int) (end.toEpochDay() - start.toEpochDay()) + 1;
            if (totalDays >= 3 || bridgeDays > 0) {
                result.add(new LongWeekend(start, end, totalDays, bridgeDays, holiday.getName()));
                markCovered(covered, start, end);
            }
        }

        return result.stream()
                .sorted(Comparator.comparing(LongWeekend::start))
                .toList();
    }

    private LocalDate expandWeekendsBackward(LocalDate start, LocalDate from) {
        while (isWeekend(start.minusDays(1)) && !start.minusDays(1).isBefore(from)) {
            start = start.minusDays(1);
        }
        return start;
    }

    private LocalDate expandWeekendsForward(LocalDate end, LocalDate to) {
        while (isWeekend(end.plusDays(1)) && !end.plusDays(1).isAfter(to)) {
            end = end.plusDays(1);
        }
        return end;
    }

    private boolean canBridgeBefore(LocalDate bridgeDay, LocalDate from, Set<LocalDate> holidayDates) {
        if (bridgeDay.isBefore(from) || isWeekend(bridgeDay) || holidayDates.contains(bridgeDay)) {
            return false;
        }
        LocalDate anchor = bridgeDay.minusDays(1);
        return !anchor.isBefore(from) && (isWeekend(anchor) || holidayDates.contains(anchor));
    }

    private boolean canBridgeAfter(LocalDate bridgeDay, LocalDate to, Set<LocalDate> holidayDates) {
        if (bridgeDay.isAfter(to) || isWeekend(bridgeDay) || holidayDates.contains(bridgeDay)) {
            return false;
        }
        LocalDate anchor = bridgeDay.plusDays(1);
        return !anchor.isAfter(to) && (isWeekend(anchor) || holidayDates.contains(anchor));
    }

    private void markCovered(Set<LocalDate> covered, LocalDate start, LocalDate end) {
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            covered.add(date);
        }
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }
}
