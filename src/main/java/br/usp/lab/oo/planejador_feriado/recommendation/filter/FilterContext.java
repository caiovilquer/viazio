package br.usp.lab.oo.planejador_feriado.recommendation.filter;

import br.usp.lab.oo.planejador_feriado.country.model.Country;
import br.usp.lab.oo.planejador_feriado.exchange.model.Exchange;
import br.usp.lab.oo.planejador_feriado.recommendation.model.RecommendationRequest;

/**
 * Dados disponíveis para a cadeia de filtros decidir se um candidato deve ser
 * descartado antes do scoring.
 */
public record FilterContext(
        String countryCode,
        Country country,
        Exchange exchangeToBrl,
        RecommendationRequest request
) {
}
