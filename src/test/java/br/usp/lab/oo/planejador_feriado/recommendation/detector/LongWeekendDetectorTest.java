package br.usp.lab.oo.planejador_feriado.recommendation.detector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.usp.lab.oo.planejador_feriado.holiday.model.Holiday;
import br.usp.lab.oo.planejador_feriado.recommendation.model.LongWeekend;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LongWeekendDetectorTest {

  private LongWeekendDetector detector;

  @BeforeEach
  void setUp() {
    detector = new LongWeekendDetector();
  }

  @Test
  void shouldDetectFourDayBridgeFromThursdayHoliday() {
    LocalDate corpusChristi = LocalDate.of(2026, 6, 4);
    Holiday holiday = new Holiday(
      corpusChristi,
      "Corpus Christi",
      "Corpus Christi",
      List.of("Public")
    );

    List<LongWeekend> result = detector.detect(
      List.of(holiday),
      LocalDate.of(2026, 6, 1),
      LocalDate.of(2026, 6, 30)
    );

    assertEquals(1, result.size());
    LongWeekend weekend = result.get(0);
    assertEquals(LocalDate.of(2026, 6, 4), weekend.start());
    assertEquals(LocalDate.of(2026, 6, 7), weekend.end());
    assertEquals(4, weekend.totalDays());
    assertEquals(1, weekend.bridgeDaysUsed());
  }

  @Test
  void shouldDetectThreeDayWeekendForFridayHoliday() {
    LocalDate fridayHoliday = LocalDate.of(2026, 5, 1);
    Holiday holiday = new Holiday(
      fridayHoliday,
      "Labour Day",
      "Dia do Trabalho",
      List.of("Public")
    );

    List<LongWeekend> result = detector.detect(
      List.of(holiday),
      LocalDate.of(2026, 5, 1),
      LocalDate.of(2026, 5, 10)
    );

    assertEquals(1, result.size());
    assertEquals(3, result.get(0).totalDays());
    assertEquals(0, result.get(0).bridgeDaysUsed());
  }

  @Test
  void shouldIgnoreSaturdayHolidayWithoutExtraGain() {
    LocalDate saturday = LocalDate.of(2026, 8, 15);
    Holiday holiday = new Holiday(
      saturday,
      "Saturday Holiday",
      "Feriado",
      List.of("Public")
    );

    List<LongWeekend> result = detector.detect(
      List.of(holiday),
      LocalDate.of(2026, 8, 1),
      LocalDate.of(2026, 8, 31)
    );

    assertTrue(result.isEmpty());
  }

  @Test
  void shouldReturnEmptyWhenNoPublicHolidaysInWindow() {
    List<LongWeekend> result = detector.detect(
      List.of(),
      LocalDate.of(2026, 6, 1),
      LocalDate.of(2026, 6, 30)
    );

    assertTrue(result.isEmpty());
  }

  @Test
  void shouldDetectBridgeBeforeTuesdayHoliday() {
    LocalDate tuesday = LocalDate.of(2026, 11, 3);
    Holiday holiday = new Holiday(
      tuesday,
      "Holiday",
      "Feriado",
      List.of("Public")
    );

    List<LongWeekend> result = detector.detect(
      List.of(holiday),
      LocalDate.of(2026, 11, 1),
      LocalDate.of(2026, 11, 10)
    );

    assertEquals(1, result.size());
    assertEquals(LocalDate.of(2026, 11, 1), result.get(0).start());
    assertEquals(tuesday, result.get(0).end());
    assertEquals(3, result.get(0).totalDays());
    assertEquals(1, result.get(0).bridgeDaysUsed());
  }
}
