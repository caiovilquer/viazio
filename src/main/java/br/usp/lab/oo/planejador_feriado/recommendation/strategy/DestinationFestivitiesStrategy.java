package br.usp.lab.oo.planejador_feriado.recommendation.strategy;

import br.usp.lab.oo.planejador_feriado.holiday.model.Holiday;
import br.usp.lab.oo.planejador_feriado.recommendation.model.Criterion;
import br.usp.lab.oo.planejador_feriado.recommendation.model.RecommendationContext;
import br.usp.lab.oo.planejador_feriado.recommendation.model.ScoreEntry;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Premia destinos que têm feriados/festividades durante a janela — uma oportunidade
 * de vivenciar a cultura local (festas, eventos). A ausência de festividades é
 * neutra (nota baixa, não zero), pois não é um defeito do destino.
 */
@Component
public class DestinationFestivitiesStrategy implements ScoringStrategy {

  @Override
  public Criterion criterion() {
    return Criterion.FESTIVITIES;
  }

  @Override
  public ScoreEntry evaluate(RecommendationContext context) {
    List<Holiday> publicHolidays = context
      .destinationHolidaysInWindow()
      .stream()
      .filter(Holiday::isPublicHoliday)
      .toList();

    int count = publicHolidays.size();
    double score = switch (Math.min(count, 4)) {
      case 0 -> 45.0;
      case 1 -> 75.0;
      case 2 -> 68.0;
      case 3 -> 55.0;
      default -> 40.0;
    };

    return ScoreEntry.of(
      criterion(),
      score,
      buildJustification(publicHolidays)
    );
  }

  private String buildJustification(List<Holiday> publicHolidays) {
    if (publicHolidays.isEmpty()) {
      return "Sem feriados nacionais no destino; menor risco de fechamentos";
    }
    String names = publicHolidays
      .stream()
      .limit(2)
      .map(Holiday::getName)
      .collect(Collectors.joining(", "));
    String suffix = publicHolidays.size() > 2 ? " e outros" : "";
    String impact =
      publicHolidays.size() == 1
        ? "oportunidade cultural com possível alteração de horários"
        : "atenção a fechamentos e maior movimento";
    return (
      publicHolidays.size() +
      " feriado(s) nacionais: " +
      names +
      suffix +
      "; " +
      impact
    );
  }
}
