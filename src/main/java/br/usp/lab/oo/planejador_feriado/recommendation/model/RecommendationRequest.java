package br.usp.lab.oo.planejador_feriado.recommendation.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Parâmetros de uma busca por recomendações.
 *
 * @param countryCodes         lista ISO de candidatos (ou vazia, se usar região)
 * @param region               região (ou {@code null}, se usar lista)
 * @param maxExchangeRate       teto de câmbio em BRL (filtro de orçamento), opcional
 * @param limit                máximo de resultados
 * @param profile              perfil de pesos escolhido (ex.: "economico"); {@code null} = padrão
 * @param weightOverrides      ajustes finos de peso por critério, sobrepõem o perfil
 * @param excludedCountryCodes códigos ISO a excluir explicitamente da comparação
 */
public record RecommendationRequest(
        LocalDate from,
        LocalDate to,
        List<String> countryCodes,
        String region,
        Double maxExchangeRate,
        int limit,
        String profile,
        Map<Criterion, Double> weightOverrides,
        List<String> excludedCountryCodes
) {

    public RecommendationRequest {
        weightOverrides = weightOverrides != null ? Map.copyOf(weightOverrides) : Map.of();
        excludedCountryCodes = excludedCountryCodes != null ? List.copyOf(excludedCountryCodes) : List.of();
    }

    /** Construtor compacto sem personalização (perfil padrão, sem exclusões). */
    public RecommendationRequest(
            LocalDate from,
            LocalDate to,
            List<String> countryCodes,
            String region,
            Double maxExchangeRate,
            int limit) {
        this(from, to, countryCodes, region, maxExchangeRate, limit, null, Map.of(), List.of());
    }
}
