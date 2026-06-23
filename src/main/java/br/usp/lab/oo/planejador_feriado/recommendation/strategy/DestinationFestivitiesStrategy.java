package br.usp.lab.oo.planejador_feriado.recommendation.strategy;

import br.usp.lab.oo.planejador_feriado.holiday.model.Holiday;
import br.usp.lab.oo.planejador_feriado.recommendation.model.Criterion;
import br.usp.lab.oo.planejador_feriado.recommendation.model.RecommendationContext;
import br.usp.lab.oo.planejador_feriado.recommendation.model.ScoreEntry;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

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
        List<Holiday> publicHolidays = context.destinationHolidaysInWindow().stream()
                .filter(Holiday::isPublicHoliday)
                .toList();

        int count = publicHolidays.size();
        double score = switch (Math.min(count, 4)) {
            case 0 -> 25.0;
            case 1 -> 55.0;
            case 2 -> 72.0;
            case 3 -> 85.0;
            default -> 100.0;
        };

        return ScoreEntry.of(criterion(), score, buildJustification(publicHolidays));
    }

    private String buildJustification(List<Holiday> publicHolidays) {
        if (publicHolidays.isEmpty()) {
            return "Sem feriados locais na janela";
        }
        String names = publicHolidays.stream()
                .limit(2)
                .map(Holiday::getName)
                .collect(Collectors.joining(", "));
        String suffix = publicHolidays.size() > 2 ? " e outros" : "";
        return publicHolidays.size() + " feriado(s) no destino: " + names + suffix;
    }
}
