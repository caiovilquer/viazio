package br.usp.lab.oo.planejador_feriado.recommendation.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Parâmetros do endpoint de "melhores janelas": dado um período amplo, encontrar os
 * melhores feriadões/pontes; opcionalmente, rankear destinos dentro de cada janela.
 *
 * @param minDays               tamanho mínimo de feriadão a considerar
 * @param topWindows            quantas janelas retornar
 * @param destinationsPerWindow quantos destinos rankear por janela (se houver candidatos)
 */
public record BestWindowsRequest(
        LocalDate from,
        LocalDate to,
        int minDays,
        int topWindows,
        List<String> countryCodes,
        String region,
        Double maxExchangeRate,
        int destinationsPerWindow,
        String profile,
        Map<Criterion, Double> weightOverrides,
        List<String> excludedCountryCodes
) {

    public boolean hasCandidates() {
        boolean hasCountries = countryCodes != null && !countryCodes.isEmpty();
        boolean hasRegion = region != null && !region.isBlank();
        return hasCountries || hasRegion;
    }
}
