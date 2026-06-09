package br.usp.lab.oo.planejador_feriado.recommendation.model;

import java.time.LocalDate;
import java.util.List;

public record RecommendationRequest(
        LocalDate from,
        LocalDate to,
        List<String> countryCodes,
        String region,
        Double maxExchangeRate,
        int limit
) {
}
