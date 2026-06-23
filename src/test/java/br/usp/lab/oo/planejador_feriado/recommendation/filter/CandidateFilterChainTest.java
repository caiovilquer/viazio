package br.usp.lab.oo.planejador_feriado.recommendation.filter;

import br.usp.lab.oo.planejador_feriado.country.model.Country;
import br.usp.lab.oo.planejador_feriado.exchange.model.Exchange;
import br.usp.lab.oo.planejador_feriado.recommendation.model.RecommendationRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CandidateFilterChainTest {

    private final CandidateFilterChain chain = new CandidateFilterChain(
            List.of(new ExcludedCountriesFilter(), new MaxExchangeRateFilter()));

    private final Country france = new Country("France", "FR", "Europe", "Western Europe",
            List.of("Paris"), List.of("French"), List.of("EUR"), List.of("UTC+01:00"));

    @Test
    void passesWhenNoFilterRejects() {
        FilterContext context = new FilterContext("FR", france, new Exchange("EUR", 6.30),
                request(null, List.of()));

        assertTrue(chain.reject(context).isEmpty());
    }

    @Test
    void rejectsExcludedCountry() {
        FilterContext context = new FilterContext("FR", france, new Exchange("EUR", 6.30),
                request(null, List.of("FR")));

        Optional<String> rejection = chain.reject(context);
        assertTrue(rejection.isPresent());
        assertTrue(rejection.get().contains("Excluído"));
    }

    @Test
    void rejectsWhenOverBudget() {
        FilterContext context = new FilterContext("FR", france, new Exchange("EUR", 6.30),
                request(3.0, List.of()));

        Optional<String> rejection = chain.reject(context);
        assertTrue(rejection.isPresent());
        assertTrue(rejection.get().contains("orçamento"));
    }

    @Test
    void passesWhenWithinBudget() {
        FilterContext context = new FilterContext("FR", france, new Exchange("EUR", 6.30),
                request(7.0, List.of()));

        assertFalse(chain.reject(context).isPresent());
    }

    private RecommendationRequest request(Double maxRate, List<String> excluded) {
        return new RecommendationRequest(
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30), List.of("FR"), null,
                maxRate, 10, null, Map.of(), excluded);
    }
}
