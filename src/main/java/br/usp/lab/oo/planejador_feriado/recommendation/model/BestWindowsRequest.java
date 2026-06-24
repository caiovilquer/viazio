package br.usp.lab.oo.planejador_feriado.recommendation.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record BestWindowsRequest(
        LocalDate from,
        LocalDate to,
        int minDays,
        int topWindows,
        List<String> countryCodes,
        String region,
        int destinationsPerWindow,
        String profile,
        Map<Criterion, Double> weightOverrides,
        List<String> excludedCountryCodes,
        String originCountryCode,
        String originSubdivisionCode,
        Double originLatitude,
        Double originLongitude,
        String originCityName,
        int travelers,
        Double maxGroundBudgetBrl
) {
    public BestWindowsRequest {
        countryCodes = countryCodes != null ? List.copyOf(countryCodes) : List.of();
        weightOverrides = weightOverrides != null ? Map.copyOf(weightOverrides) : Map.of();
        excludedCountryCodes = excludedCountryCodes != null ? List.copyOf(excludedCountryCodes) : List.of();
        originCountryCode = originCountryCode == null || originCountryCode.isBlank()
                ? "BR"
                : originCountryCode;
        travelers = travelers > 0 ? travelers : 1;
    }

    public boolean hasCandidates() {
        return !countryCodes.isEmpty() || (region != null && !region.isBlank());
    }
}
