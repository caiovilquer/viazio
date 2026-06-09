package br.usp.lab.oo.planejador_feriado.recommendation.controller;

import br.usp.lab.oo.planejador_feriado.recommendation.dto.RecommendationResponse;
import br.usp.lab.oo.planejador_feriado.recommendation.model.RecommendationRequest;
import br.usp.lab.oo.planejador_feriado.recommendation.service.TravelRecommendationEngine;
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
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 15;
    private static final int MAX_WINDOW_DAYS = 92;

    private final TravelRecommendationEngine recommendationEngine;

    public RecommendationController(TravelRecommendationEngine recommendationEngine) {
        this.recommendationEngine = recommendationEngine;
    }

    @GetMapping
    public RecommendationResponse getRecommendations(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String countries,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) Double maxRate,
            @RequestParam(required = false, defaultValue = "10") int limit) {

        validateWindow(from, to);
        validateCandidateInput(countries, region);
        validateLimit(limit);

        List<String> countryCodes = parseCountries(countries);
        RecommendationRequest request = new RecommendationRequest(
                from,
                to,
                countryCodes,
                normalizeRegion(region),
                maxRate,
                limit
        );

        return recommendationEngine.recommend(request);
    }

    private void validateWindow(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parâmetros 'from' e 'to' são obrigatórios");
        }
        if (from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'from' deve ser anterior ou igual a 'to'");
        }
        long days = ChronoUnit.DAYS.between(from, to) + 1;
        if (days > MAX_WINDOW_DAYS) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Janela máxima permitida é de " + MAX_WINDOW_DAYS + " dias"
            );
        }
    }

    private void validateCandidateInput(String countries, String region) {
        boolean hasCountries = countries != null && !countries.isBlank();
        boolean hasRegion = region != null && !region.isBlank();

        if (hasCountries == hasRegion) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe exatamente um entre 'countries' (lista ISO) ou 'region'"
            );
        }
    }

    private void validateLimit(int limit) {
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "'limit' deve estar entre 1 e " + MAX_LIMIT
            );
        }
    }

    private List<String> parseCountries(String countries) {
        if (countries == null || countries.isBlank()) {
            return List.of();
        }

        return Arrays.stream(countries.split(","))
                .map(String::trim)
                .filter(code -> !code.isBlank())
                .map(code -> code.toUpperCase(Locale.ROOT))
                .collect(Collectors.toList());
    }

    private String normalizeRegion(String region) {
        if (region == null || region.isBlank()) {
            return null;
        }
        return region.trim();
    }
}
