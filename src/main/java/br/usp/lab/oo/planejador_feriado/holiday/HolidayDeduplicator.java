package br.usp.lab.oo.planejador_feriado.holiday;

import br.usp.lab.oo.planejador_feriado.holiday.model.Holiday;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Remove feriados duplicados com base na chave data + nome.
 * Classe pura, reutilizada por {@link br.usp.lab.oo.planejador_feriado.travel.service.TravelService}
 * e pelo motor de recomendação.
 */
public final class HolidayDeduplicator {

  private HolidayDeduplicator() {}

  public static List<Holiday> deduplicate(List<Holiday> holidays) {
    if (holidays == null || holidays.isEmpty()) {
      return List.of();
    }

    List<Holiday> clean = new ArrayList<>();
    Set<String> seen = new HashSet<>();

    for (Holiday holiday : holidays) {
      String key = holiday.getDate().toString() + "-" + holiday.getName();
      if (!seen.contains(key)) {
        clean.add(holiday);
        seen.add(key);
      }
    }

    return clean;
  }
}
