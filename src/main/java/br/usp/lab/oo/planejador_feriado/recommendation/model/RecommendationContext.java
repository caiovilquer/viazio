package br.usp.lab.oo.planejador_feriado.recommendation.model;

import br.usp.lab.oo.planejador_feriado.cost.model.CostOfLiving;
import br.usp.lab.oo.planejador_feriado.country.model.Country;
import br.usp.lab.oo.planejador_feriado.enrichment.model.DestinationProfile;
import br.usp.lab.oo.planejador_feriado.exchange.model.Exchange;
import br.usp.lab.oo.planejador_feriado.holiday.model.Holiday;
import br.usp.lab.oo.planejador_feriado.weather.model.WeatherSummary;

import java.util.List;

/** Dados de um destino já coletados antes da execução das strategies. */
public record RecommendationContext(
        Country country,
        List<Holiday> destinationHolidaysInWindow,
        Exchange exchangeToBrl,
        WeatherSummary weather,
        CostOfLiving destinationCost,
        CostOfLiving originCost,
        Double distanceFromOriginKm,
        DestinationProfile profile,
        RecommendationRequest request
) {
}
