package br.usp.lab.oo.planejador_feriado.recommendation.model;

import br.usp.lab.oo.planejador_feriado.country.model.Country;
import br.usp.lab.oo.planejador_feriado.exchange.model.Exchange;
import br.usp.lab.oo.planejador_feriado.holiday.model.Holiday;

import java.util.List;

public record RecommendationContext(
        Country country,
        List<Holiday> destinationHolidaysInWindow,
        List<Holiday> brazilHolidaysInWindow,
        List<LongWeekend> brazilLongWeekends,
        Exchange exchangeToBrl,
        RecommendationRequest request
) {
}
