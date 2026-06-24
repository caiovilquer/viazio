package br.usp.lab.oo.planejador_feriado.recommendation.model;

import br.usp.lab.oo.planejador_feriado.enrichment.model.DestinationProfile;

import java.util.List;

/**
 * Recomendação de um destino: score final 0–100 (média ponderada dos critérios
 * disponíveis), breakdown explicável, "highlights" curtos para chips na UI, um
 * resumo de uma linha e um perfil descritivo (bandeira/imagem/texto da Wikipédia)
 * que não participa do score — é só para a UI mostrar o destino de forma vívida.
 */
public record TravelRecommendation(
        String countryCode,
        String countryName,
        double score,
        List<ScoredCriterion> breakdown,
        List<String> highlights,
        String summary,
        DestinationProfile profile
) {
}
