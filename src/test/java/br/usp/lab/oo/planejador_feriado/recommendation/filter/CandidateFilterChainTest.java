package br.usp.lab.oo.planejador_feriado.recommendation.filter;

import br.usp.lab.oo.planejador_feriado.country.model.Country;
import br.usp.lab.oo.planejador_feriado.recommendation.model.GroundCostEstimate;
import br.usp.lab.oo.planejador_feriado.recommendation.model.RecommendationRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CandidateFilterChainTest {

    private final CandidateFilterChain chain = new CandidateFilterChain(
            List.of(new ExcludedCountriesFilter(), new MaxGroundBudgetFilter()));
    private final Country france = new Country("France", "FR", "Europe", "Western Europe",
            List.of("Paris"), List.of("French"), List.of("EUR"), List.of("UTC+01:00"));

    @Test
    void passesWhenCountryIsAllowed() {
        assertTrue(chain.reject(new FilterContext("FR", france, null, request(List.of()))).isEmpty());
    }

    @Test
    void rejectsExcludedCountry() {
        var rejection = chain.reject(new FilterContext("FR", france, null, request(List.of("FR"))));
        assertTrue(rejection.isPresent());
        assertTrue(rejection.orElseThrow().contains("Excluído"));
    }

    @Test
    void rejectsEstimateAboveGroundBudget() {
        GroundCostEstimate estimate = new GroundCostEstimate(
                "BRL", 500.0, 5_000.0, 1, 10, 1.4,
                "2024", "2024", "LOW", "PPP");

        var rejection = chain.reject(new FilterContext(
                "FR", france, estimate, request(List.of(), 4_000.0)));

        assertTrue(rejection.isPresent());
        assertTrue(rejection.orElseThrow().contains("acima do orçamento"));
    }

    @Test
    void passesEstimateWithinGroundBudget() {
        GroundCostEstimate estimate = new GroundCostEstimate(
                "BRL", 300.0, 3_000.0, 1, 10, 0.9,
                "2024", "2024", "LOW", "PPP");

        assertTrue(chain.reject(new FilterContext(
                "FR", france, estimate, request(List.of(), 4_000.0))).isEmpty());
    }

    private RecommendationRequest request(List<String> excluded) {
        return request(excluded, null);
    }

    private RecommendationRequest request(List<String> excluded, Double maxGroundBudget) {
        return new RecommendationRequest(
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 30),
                List.of("FR"),
                null,
                10,
                null,
                Map.of(),
                excluded,
                "BR",
                null,
                null,
                null,
                null,
                1,
                maxGroundBudget);
    }
}
