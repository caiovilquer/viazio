package br.usp.lab.oo.planejador_feriado.recommendation.model;

import java.util.List;

public record TravelRecommendation(
        String countryCode,
        String countryName,
        double score,
        List<ScoreEntry> breakdown,
        String summary
) {
}
