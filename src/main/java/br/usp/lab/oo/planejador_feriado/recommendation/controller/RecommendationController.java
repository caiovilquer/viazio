package br.usp.lab.oo.planejador_feriado.recommendation.controller;

import br.usp.lab.oo.planejador_feriado.recommendation.dto.BestWindowsResponse;
import br.usp.lab.oo.planejador_feriado.recommendation.dto.RecommendationResponse;
import br.usp.lab.oo.planejador_feriado.recommendation.model.BestWindowsRequest;
import br.usp.lab.oo.planejador_feriado.recommendation.model.Criterion;
import br.usp.lab.oo.planejador_feriado.recommendation.model.RecommendationRequest;
import br.usp.lab.oo.planejador_feriado.recommendation.service.BestWindowsService;
import br.usp.lab.oo.planejador_feriado.recommendation.service.TravelRecommendationEngine;
import br.usp.lab.oo.planejador_feriado.recommendation.weight.WeightResolver;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/recommendations")
public class RecommendationController {

    private static final int MAX_LIMIT = 15;
    private static final int MAX_WINDOW_DAYS = 92;
    private static final int MAX_PERIOD_DAYS = 400;

    private final TravelRecommendationEngine recommendationEngine;
    private final BestWindowsService bestWindowsService;
    private final WeightResolver weightResolver;

    public RecommendationController(
            TravelRecommendationEngine recommendationEngine,
            BestWindowsService bestWindowsService,
            WeightResolver weightResolver) {
        this.recommendationEngine = recommendationEngine;
        this.bestWindowsService = bestWindowsService;
        this.weightResolver = weightResolver;
    }

    @GetMapping
    public RecommendationResponse getRecommendations(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String countries,
            @RequestParam(required = false) String region,
            @RequestParam(required = false, defaultValue = "10") int limit,
            @RequestParam(required = false) String profile,
            @RequestParam(required = false) String weights,
            @RequestParam(required = false) String exclude,
            @RequestParam(required = false, defaultValue = "BR") String originCountry,
            @RequestParam(required = false) String originSubdivision,
            @RequestParam(required = false) Double originLatitude,
            @RequestParam(required = false) Double originLongitude,
            @RequestParam(required = false) String originCity,
            @RequestParam(required = false, defaultValue = "1") int travelers,
            @RequestParam(required = false) Double maxGroundBudget) {

        validateWindow(from, to, MAX_WINDOW_DAYS);
        validateCandidateInput(countries, region);
        validateLimit(limit, MAX_LIMIT);
        validateProfile(profile);
        validateCoordinates(originLatitude, originLongitude);
        validateRange("travelers", travelers, 1, 10);
        validatePositiveMoney("maxGroundBudget", maxGroundBudget);

        RecommendationRequest request = new RecommendationRequest(
                from,
                to,
                parseCountries(countries),
                normalizeRegion(region),
                limit,
                normalizeProfile(profile),
                parseWeights(weights),
                parseCodes(exclude),
                normalizeCountryCode(originCountry, "originCountry"),
                normalizeSubdivision(originSubdivision),
                originLatitude,
                originLongitude,
                normalizeText(originCity),
                travelers,
                maxGroundBudget
        );

        return recommendationEngine.recommend(request);
    }

    @GetMapping("/best-windows")
    public BestWindowsResponse getBestWindows(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false, defaultValue = "3") int minDays,
            @RequestParam(required = false, defaultValue = "6") int topWindows,
            @RequestParam(required = false) String countries,
            @RequestParam(required = false) String region,
            @RequestParam(required = false, defaultValue = "3") int destinationsPerWindow,
            @RequestParam(required = false) String profile,
            @RequestParam(required = false) String weights,
            @RequestParam(required = false) String exclude,
            @RequestParam(required = false, defaultValue = "BR") String originCountry,
            @RequestParam(required = false) String originSubdivision,
            @RequestParam(required = false) Double originLatitude,
            @RequestParam(required = false) Double originLongitude,
            @RequestParam(required = false) String originCity,
            @RequestParam(required = false, defaultValue = "1") int travelers,
            @RequestParam(required = false) Double maxGroundBudget) {

        validateWindow(from, to, MAX_PERIOD_DAYS);
        validateOptionalCandidateInput(countries, region);
        validateRange("minDays", minDays, 3, 30);
        validateRange("topWindows", topWindows, 1, 20);
        validateRange("destinationsPerWindow", destinationsPerWindow, 1, MAX_LIMIT);
        validateProfile(profile);
        validateCoordinates(originLatitude, originLongitude);
        validateRange("travelers", travelers, 1, 10);
        validatePositiveMoney("maxGroundBudget", maxGroundBudget);

        BestWindowsRequest request = new BestWindowsRequest(
                from,
                to,
                minDays,
                topWindows,
                parseCountries(countries),
                normalizeRegion(region),
                destinationsPerWindow,
                normalizeProfile(profile),
                parseWeights(weights),
                parseCodes(exclude),
                normalizeCountryCode(originCountry, "originCountry"),
                normalizeSubdivision(originSubdivision),
                originLatitude,
                originLongitude,
                normalizeText(originCity),
                travelers,
                maxGroundBudget
        );

        return bestWindowsService.findBestWindows(request);
    }

    private void validateWindow(LocalDate from, LocalDate to, int maxDays) {
        if (from == null || to == null) {
            throw badRequest("Parâmetros 'from' e 'to' são obrigatórios");
        }
        if (from.isAfter(to)) {
            throw badRequest("'from' deve ser anterior ou igual a 'to'");
        }
        long days = ChronoUnit.DAYS.between(from, to) + 1;
        if (days > maxDays) {
            throw badRequest("Janela máxima permitida é de " + maxDays + " dias");
        }
    }

    private void validateCandidateInput(String countries, String region) {
        boolean hasCountries = countries != null && !countries.isBlank();
        boolean hasRegion = region != null && !region.isBlank();
        if (hasCountries == hasRegion) {
            throw badRequest("Informe exatamente um entre 'countries' (lista ISO) ou 'region'");
        }
    }

    private void validateOptionalCandidateInput(String countries, String region) {
        boolean hasCountries = countries != null && !countries.isBlank();
        boolean hasRegion = region != null && !region.isBlank();
        if (hasCountries && hasRegion) {
            throw badRequest("Use 'countries' ou 'region', não ambos");
        }
    }

    private void validateLimit(int limit, int max) {
        validateRange("limit", limit, 1, max);
    }

    private void validateRange(String name, int value, int min, int max) {
        if (value < min || value > max) {
            throw badRequest("'" + name + "' deve estar entre " + min + " e " + max);
        }
    }

    private void validateProfile(String profile) {
        if (profile != null && !profile.isBlank() && !weightResolver.isKnownProfile(profile)) {
            throw badRequest("Perfil desconhecido. Disponíveis: " + String.join(", ", weightResolver.availableProfiles()));
        }
    }

    private String normalizeProfile(String profile) {
        return profile != null && !profile.isBlank() ? profile.trim().toLowerCase(Locale.ROOT) : null;
    }

    private List<String> parseCountries(String countries) {
        return parseCodes(countries);
    }

    private List<String> parseCodes(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(code -> !code.isBlank())
                .map(code -> code.toUpperCase(Locale.ROOT))
                .peek(code -> {
                    if (!code.matches("[A-Z]{2}")) {
                        throw badRequest("Código de país inválido: " + code);
                    }
                })
                .collect(Collectors.toList());
    }

    private Map<Criterion, Double> parseWeights(String weights) {
        if (weights == null || weights.isBlank()) {
            return Map.of();
        }
        Map<Criterion, Double> parsed = new EnumMap<>(Criterion.class);
        for (String pair : weights.split(",")) {
            String[] parts = pair.split(":");
            if (parts.length != 2) {
                throw badRequest("Formato de 'weights' inválido. Use criterio:valor,criterio:valor (ex.: clima:0.4,custo:0.2)");
            }
            Criterion criterion = Criterion.fromKey(parts[0].trim())
                    .orElseThrow(() -> badRequest("Critério desconhecido em 'weights': " + parts[0].trim()
                            + ". Válidos: " + validCriteriaKeys()));
            double value;
            try {
                value = Double.parseDouble(parts[1].trim());
            } catch (NumberFormatException e) {
                throw badRequest("Peso inválido em 'weights' para " + parts[0].trim() + ": " + parts[1].trim());
            }
            if (!Double.isFinite(value) || value < 0) {
                throw badRequest("Pesos não podem ser negativos em 'weights'");
            }
            parsed.put(criterion, value);
        }
        return parsed;
    }

    private String validCriteriaKeys() {
        return Arrays.stream(Criterion.values()).map(Criterion::key).collect(Collectors.joining(", "));
    }

    private String normalizeRegion(String region) {
        return region != null && !region.isBlank() ? region.trim() : null;
    }

    private String normalizeCountryCode(String value, String parameter) {
        String code = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
        if (!code.matches("[A-Z]{2}")) {
            throw badRequest("'" + parameter + "' deve ser um código ISO alpha-2");
        }
        return code;
    }

    private String normalizeSubdivision(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String subdivision = value.trim().toUpperCase(Locale.ROOT);
        if (!subdivision.matches("[A-Z]{2}-[A-Z0-9]{1,3}")) {
            throw badRequest("'originSubdivision' deve usar ISO 3166-2 (ex.: BR-SP)");
        }
        return subdivision;
    }

    private void validateCoordinates(Double latitude, Double longitude) {
        if ((latitude == null) != (longitude == null)) {
            throw badRequest("'originLatitude' e 'originLongitude' devem ser informadas juntas");
        }
        if (latitude != null && (!Double.isFinite(latitude) || latitude < -90.0 || latitude > 90.0)) {
            throw badRequest("'originLatitude' deve estar entre -90 e 90");
        }
        if (longitude != null && (!Double.isFinite(longitude) || longitude < -180.0 || longitude > 180.0)) {
            throw badRequest("'originLongitude' deve estar entre -180 e 180");
        }
    }

    private void validatePositiveMoney(String parameter, Double value) {
        if (value != null && (!Double.isFinite(value) || value <= 0.0)) {
            throw badRequest("'" + parameter + "' deve ser um valor positivo");
        }
    }

    private String normalizeText(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
