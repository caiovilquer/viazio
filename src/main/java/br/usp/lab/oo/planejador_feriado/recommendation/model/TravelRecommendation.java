package br.usp.lab.oo.planejador_feriado.recommendation.model;

import java.util.List;

/**
 * Recomendação de um destino: score final 0–100 (média ponderada dos critérios
 * disponíveis), breakdown explicável, "highlights" curtos para chips na UI e um
 * resumo de uma linha.
 */
public record TravelRecommendation(
        String countryCode,
        String countryName,
        double score,
        List<ScoredCriterion> breakdown,
        List<String> highlights,
        String summary
) {
}
