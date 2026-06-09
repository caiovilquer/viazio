package br.usp.lab.oo.planejador_feriado.cli;

import br.usp.lab.oo.planejador_feriado.country.model.Country;
import br.usp.lab.oo.planejador_feriado.country.service.CountryService;
import br.usp.lab.oo.planejador_feriado.exchange.model.Exchange;
import br.usp.lab.oo.planejador_feriado.exchange.service.ExchangeService;
import br.usp.lab.oo.planejador_feriado.holiday.model.Holiday;
import br.usp.lab.oo.planejador_feriado.holiday.service.HolidayService;
import br.usp.lab.oo.planejador_feriado.travel.model.TravelOverview;
import br.usp.lab.oo.planejador_feriado.travel.service.TravelService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanejadorShellCommandsTest {

    @Mock
    private CountryService countryService;

    @Mock
    private HolidayService holidayService;

    @Mock
    private ExchangeService exchangeService;

    @Mock
    private TravelService travelService;

    @InjectMocks
    private PlanejadorShellCommands commands;

    private Country japan() {
        return new Country("Japan", "JP", "Asia", "Eastern Asia",
                List.of("Tokyo"), List.of("Japanese"), List.of("JPY"), List.of("UTC+09:00"));
    }

    @Test
    void countryByCodeShouldTrimAndReturnCountryDescription() {
        when(countryService.getCountryByCode("JP")).thenReturn(japan());

        String output = commands.countryByCode("  JP  ");

        assertTrue(output.contains("Japan"));
        assertTrue(output.contains("JP"));
    }

    @Test
    void countryByNameShouldReturnCountryDescription() {
        when(countryService.getCountryByName("japan")).thenReturn(japan());

        String output = commands.countryByName("japan");

        assertTrue(output.contains("Japan"));
    }

    @Test
    void holidaysShouldListUpcomingHolidays() {
        Country country = japan();
        Holiday holiday = new Holiday(LocalDate.of(2026, 12, 25), "Christmas Day", "Natal", List.of("Public"));
        when(countryService.getCountryByCode("BR")).thenReturn(country);
        when(holidayService.getUpcomingHolidays(country)).thenReturn(List.of(holiday));

        String output = commands.holidays("BR");

        assertTrue(output.contains("Christmas Day"));
    }

    @Test
    void holidaysShouldReturnPlaceholderWhenEmpty() {
        Country country = japan();
        when(countryService.getCountryByCode("BR")).thenReturn(country);
        when(holidayService.getUpcomingHolidays(country)).thenReturn(List.of());

        String output = commands.holidays("BR");

        assertEquals("(nenhum feriado futuro)", output);
    }

    @Test
    void exchangeShouldReturnFormattedRate() {
        when(exchangeService.getExchangeRate("USD")).thenReturn(new Exchange("USD", 5.12));

        String output = commands.exchange("USD");

        assertTrue(output.contains("USD"));
    }

    @Test
    void travelShouldAggregateCountryHolidaysAndExchange() {
        Holiday holiday = new Holiday(LocalDate.of(2026, 12, 25), "Christmas Day", "Natal", List.of("Public"));
        TravelOverview overview = new TravelOverview(japan(), List.of(holiday), new Exchange("JPY", 0.035));
        when(travelService.getOverviewByCountryCode("JP")).thenReturn(overview);

        String output = commands.travel("JP");

        assertTrue(output.contains("--- País ---"));
        assertTrue(output.contains("Japan"));
        assertTrue(output.contains("Christmas Day"));
        assertTrue(output.contains("--- Câmbio para BRL ---"));
        assertFalse(output.contains("(não aplicável ou indisponível)"));
    }

    @Test
    void travelShouldShowPlaceholdersWhenNoHolidaysAndNoExchange() {
        TravelOverview overview = new TravelOverview(japan(), List.of(), null);
        when(travelService.getOverviewByCountryCode("JP")).thenReturn(overview);

        String output = commands.travel("JP");

        assertTrue(output.contains("(nenhum)"));
        assertTrue(output.contains("(não aplicável ou indisponível)"));
    }

    @Test
    void shouldPropagateErrorWhenCountryNotFound() {
        when(countryService.getCountryByCode("ZZ")).thenThrow(new RuntimeException("Country not found"));

        assertThrows(RuntimeException.class, () -> commands.countryByCode("ZZ"));
    }
}
