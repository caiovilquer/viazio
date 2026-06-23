package br.usp.lab.oo.planejador_feriado.recommendation.model;

import br.usp.lab.oo.planejador_feriado.cost.model.CostOfLiving;
import br.usp.lab.oo.planejador_feriado.country.model.Country;
import br.usp.lab.oo.planejador_feriado.exchange.model.Exchange;
import br.usp.lab.oo.planejador_feriado.holiday.model.Holiday;
import br.usp.lab.oo.planejador_feriado.weather.model.WeatherSummary;

import java.util.List;

/**
 * Reúne todos os dados já coletados para um candidato. As strategies são funções
 * puras sobre este contexto — todo o I/O externo (país, feriados, câmbio, clima,
 * custo, distância) acontece no motor, o que mantém as regras de score testáveis
 * sem mocks de HTTP. Campos de enriquecimento (clima/custo/distância) podem ser
 * {@code null} quando a API externa correspondente não respondeu.
 */
public record RecommendationContext(
        Country country,
        List<Holiday> destinationHolidaysInWindow,
        List<Holiday> brazilHolidaysInWindow,
        List<LongWeekend> brazilLongWeekends,
        Exchange exchangeToBrl,
        WeatherSummary weather,
        CostOfLiving destinationCost,
        CostOfLiving brazilCost,
        Double distanceFromBrazilKm,
        RecommendationRequest request
) {
}
