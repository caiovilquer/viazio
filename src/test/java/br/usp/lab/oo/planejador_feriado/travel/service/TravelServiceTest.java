package br.usp.lab.oo.planejador_feriado.travel.service;

import br.usp.lab.oo.planejador_feriado.country.model.Country;
import br.usp.lab.oo.planejador_feriado.country.service.CountryService;
import br.usp.lab.oo.planejador_feriado.exchange.model.Exchange;
import br.usp.lab.oo.planejador_feriado.exchange.service.ExchangeService;
import br.usp.lab.oo.planejador_feriado.holiday.model.Holiday;
import br.usp.lab.oo.planejador_feriado.holiday.service.HolidayService;
import br.usp.lab.oo.planejador_feriado.travel.model.TravelOverview;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TravelServiceTest {

    @Mock
    private CountryService countryService;

    @Mock
    private HolidayService holidayService;

    @Mock
    private ExchangeService exchangeService;

    @InjectMocks
    private TravelService travelService;

    @Test
    void shouldDeduplicateHolidaysAndReturnOverview() {
        Country japan = new Country("Japan", "JP", "Asia", "Eastern Asia",
                List.of("Tokyo"), List.of("Japanese"), List.of("JPY"), List.of("UTC+09:00"));
        LocalDate date = LocalDate.of(2026, 2, 11);
        Holiday holiday = new Holiday(date, "National Foundation Day", "建国記念の日", List.of("Public"));
        Holiday duplicate = new Holiday(date, "National Foundation Day", "dup", List.of("Public"));

        when(countryService.getCountryByCode("JP")).thenReturn(japan);
        when(holidayService.getUpcomingHolidays(japan)).thenReturn(List.of(holiday, duplicate));
        when(exchangeService.getExchangeRate("JPY")).thenReturn(new Exchange("JPY", 0.035));

        TravelOverview overview = travelService.getOverviewByCountryCode("JP");

        assertEquals(japan, overview.country());
        assertEquals(1, overview.upcomingHolidays().size());
        assertEquals(0.035, overview.exchangeToBrl().getValueInReais());
        verify(countryService).getCountryByCode("JP");
    }

    @Test
    void shouldReturnNullExchangeForBrlCountry() {
        Country brazil = new Country("Brazil", "BR", "Americas", "South America",
                List.of("Brasília"), List.of("Portuguese"), List.of("BRL"), List.of("UTC-03:00"));

        when(countryService.getCountryByCode("BR")).thenReturn(brazil);
        when(holidayService.getUpcomingHolidays(brazil)).thenReturn(List.of());

        TravelOverview overview = travelService.getOverviewByCountryCode("BR");

        assertNull(overview.exchangeToBrl());
    }

    @Test
    void shouldTolerateExchangeFailure() {
        Country france = new Country("France", "FR", "Europe", "Western Europe",
                List.of("Paris"), List.of("French"), List.of("EUR"), List.of("UTC+01:00"));

        when(countryService.getCountryByCode("FR")).thenReturn(france);
        when(holidayService.getUpcomingHolidays(france)).thenReturn(List.of());
        when(exchangeService.getExchangeRate("EUR")).thenThrow(new RuntimeException("offline"));

        TravelOverview overview = travelService.getOverviewByCountryCode("FR");

        assertNull(overview.exchangeToBrl());
    }
}
