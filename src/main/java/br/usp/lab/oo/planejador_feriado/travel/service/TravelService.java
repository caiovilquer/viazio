package br.usp.lab.oo.planejador_feriado.travel.service;

import br.usp.lab.oo.planejador_feriado.country.model.Country;
import br.usp.lab.oo.planejador_feriado.country.service.CountryService;
import br.usp.lab.oo.planejador_feriado.exchange.model.Exchange;
import br.usp.lab.oo.planejador_feriado.exchange.service.ExchangeService;
import br.usp.lab.oo.planejador_feriado.holiday.HolidayDeduplicator;
import br.usp.lab.oo.planejador_feriado.holiday.model.Holiday;
import br.usp.lab.oo.planejador_feriado.holiday.service.HolidayService;
import br.usp.lab.oo.planejador_feriado.travel.model.TravelOverview;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Facade que agrega país, feriados futuros e câmbio para BRL em uma única operação.
 * Esconde a orquestração de CountryService, HolidayService e ExchangeService.
 */
@Service
public class TravelService {

    private final CountryService countryService;
    private final HolidayService holidayService;
    private final ExchangeService exchangeService;

    public TravelService(
            CountryService countryService,
            HolidayService holidayService,
            ExchangeService exchangeService) {
        this.countryService = countryService;
        this.holidayService = holidayService;
        this.exchangeService = exchangeService;
    }

public TravelOverview getOverviewByCountryCode(String countryCode) {
        Country country = countryService.getCountryByCode(countryCode.trim());
        List<Holiday> rawHolidays = holidayService.getUpcomingHolidays(country);
        List<Holiday> cleanHolidays = HolidayDeduplicator.deduplicate(rawHolidays);
        Exchange exchangeToBrl = resolveExchangeToBrl(country);
        return new TravelOverview(country, cleanHolidays, exchangeToBrl);
    }

    private Exchange resolveExchangeToBrl(Country country) {
        String currency = country.getMainCurrency();
        if (currency == null || currency.isBlank() || currency.equalsIgnoreCase("BRL")) {
            return null;
        }
        try {
            return exchangeService.getExchangeRate(currency);
        } catch (RuntimeException e) {
            return null;
        }
    }
}
