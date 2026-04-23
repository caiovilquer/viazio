package br.usp.lab.oo.planejador_feriado.travel.model;

import br.usp.lab.oo.planejador_feriado.country.model.Country;
import br.usp.lab.oo.planejador_feriado.exchange.model.Exchange;
import br.usp.lab.oo.planejador_feriado.holiday.model.Holiday;

import java.util.List;

public record TravelOverview(
        Country country,
        List<Holiday> upcomingHolidays,
        Exchange exchangeToBrl
) {}
