package br.usp.lab.oo.planejador_feriado.recommendation.dto;

import br.usp.lab.oo.planejador_feriado.recommendation.model.TravelRecommendation;

import java.time.LocalDate;
import java.util.List;

/**
 * Uma janela de viagem sugerida (feriadão/ponte do calendário BR), com nota de
 * "qualidade do timing" e, opcionalmente, os melhores destinos para aquela janela.
 *
 * @param label          rótulo amigável (ex.: "Feriadão de 4 dias (Carnaval + ponte)")
 * @param timingScore    qualidade do feriadão (0–100)
 * @param topDestinations melhores destinos avaliados para esta janela (pode ser vazio)
 */
public record WindowSuggestion(
        LocalDate start,
        LocalDate end,
        int totalDays,
        int bridgeDaysUsed,
        String label,
        double timingScore,
        List<TravelRecommendation> topDestinations
) {
}
