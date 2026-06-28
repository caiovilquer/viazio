package br.usp.lab.oo.planejador_feriado.holiday;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.usp.lab.oo.planejador_feriado.holiday.model.Holiday;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class HolidayDeduplicatorTest {

  @Test
  void shouldRemoveDuplicatesByDateAndName() {
    LocalDate date = LocalDate.of(2026, 6, 4);
    Holiday first = new Holiday(
      date,
      "Corpus Christi",
      "Corpus Christi",
      List.of("Public")
    );
    Holiday duplicate = new Holiday(
      date,
      "Corpus Christi",
      "Outro nome",
      List.of("Public")
    );

    List<Holiday> result = HolidayDeduplicator.deduplicate(
      List.of(first, duplicate)
    );

    assertEquals(1, result.size());
    assertEquals(first, result.get(0));
  }

  @Test
  void shouldKeepDifferentHolidaysOnSameDate() {
    LocalDate date = LocalDate.of(2026, 12, 25);
    Holiday christmas = new Holiday(
      date,
      "Christmas",
      "Natal",
      List.of("Public")
    );
    Holiday other = new Holiday(date, "Other", "Outro", List.of("Public"));

    List<Holiday> result = HolidayDeduplicator.deduplicate(
      List.of(christmas, other)
    );

    assertEquals(2, result.size());
  }

  @Test
  void shouldReturnEmptyForNullOrEmptyInput() {
    assertTrue(HolidayDeduplicator.deduplicate(null).isEmpty());
    assertTrue(HolidayDeduplicator.deduplicate(List.of()).isEmpty());
  }
}
