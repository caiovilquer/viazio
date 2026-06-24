package br.usp.lab.oo.planejador_feriado.travel.service;

import br.usp.lab.oo.planejador_feriado.country.model.Country;
import br.usp.lab.oo.planejador_feriado.country.service.CountryService;
import br.usp.lab.oo.planejador_feriado.enrichment.model.DestinationProfile;
import br.usp.lab.oo.planejador_feriado.enrichment.service.DestinationProfileService;
import br.usp.lab.oo.planejador_feriado.exchange.model.Exchange;
import br.usp.lab.oo.planejador_feriado.exchange.service.ExchangeService;
import br.usp.lab.oo.planejador_feriado.holiday.HolidayDeduplicator;
import br.usp.lab.oo.planejador_feriado.holiday.model.Holiday;
import br.usp.lab.oo.planejador_feriado.holiday.service.HolidayService;
import br.usp.lab.oo.planejador_feriado.travel.model.TravelOverview;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Facade que agrega país, feriados futuros, câmbio para BRL e perfil descritivo
 * (população + Wikipédia) em uma única operação. Esconde a orquestração de
 * CountryService, HolidayService, ExchangeService e DestinationProfileService.
 */
@Service
public class TravelService {

    private final CountryService countryService;
    private final HolidayService holidayService;
    private final ExchangeService exchangeService;
    private final DestinationProfileService profileService;

    public TravelService(
            CountryService countryService,
            HolidayService holidayService,
            ExchangeService exchangeService,
            DestinationProfileService profileService) {
        this.countryService = countryService;
        this.holidayService = holidayService;
        this.exchangeService = exchangeService;
        this.profileService = profileService;
    }

public TravelOverview getOverviewByCountryCode(String countryCode) {
        return buildOverview(countryService.getCountryByCode(countryCode.trim()));
    }

    public TravelOverview getOverviewByQuery(String query) {
        return buildOverview(countryService.getCountryByQuery(query.trim()));
    }

    private TravelOverview buildOverview(Country country) {
        List<Holiday> rawHolidays = holidayService.getUpcomingHolidays(country);
        List<Holiday> cleanHolidays = HolidayDeduplicator.deduplicate(rawHolidays);
        Exchange exchangeToBrl = resolveExchangeToBrl(country);
        DestinationProfile profile = resolveProfile(country);
        return new TravelOverview(country, cleanHolidays, exchangeToBrl, profile);
    }

    private DestinationProfile resolveProfile(Country country) {
        try {
            return profileService.buildProfile(country);
        } catch (RuntimeException e) {
            return null;
        }
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
