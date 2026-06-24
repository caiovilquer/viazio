package br.usp.lab.oo.planejador_feriado.recommendation.filter;

import br.usp.lab.oo.planejador_feriado.country.model.Country;
import br.usp.lab.oo.planejador_feriado.recommendation.model.RecommendationRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CandidateFilterChainTest {

    private final CandidateFilterChain chain = new CandidateFilterChain(
            List.of(new ExcludedCountriesFilter()));
    private final Country france = new Country("France", "FR", "Europe", "Western Europe",
            List.of("Paris"), List.of("French"), List.of("EUR"), List.of("UTC+01:00"));

    @Test
    void passesWhenCountryIsAllowed() {
        assertTrue(chain.reject(new FilterContext("FR", france, request(List.of()))).isEmpty());
    }

    @Test
    void rejectsExcludedCountry() {
        var rejection = chain.reject(new FilterContext("FR", france, request(List.of("FR"))));
        assertTrue(rejection.isPresent());
        assertTrue(rejection.orElseThrow().contains("Excluído"));
    }

    private RecommendationRequest request(List<String> excluded) {
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
                null);
    }
}
