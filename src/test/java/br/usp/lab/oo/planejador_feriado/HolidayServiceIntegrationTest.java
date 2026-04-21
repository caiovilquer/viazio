package br.usp.lab.oo.planejador_feriado;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import br.usp.lab.oo.planejador_feriado.country.model.Country;
import br.usp.lab.oo.planejador_feriado.country.service.CountryService;
import br.usp.lab.oo.planejador_feriado.holiday.model.Holiday;
import br.usp.lab.oo.planejador_feriado.holiday.service.HolidayService;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class HolidayServiceIntegrationTest {

    @Autowired
    private CountryService countryService;

    @Autowired
    private HolidayService service;

    @Test
    void shouldReturnHolidaysForBrazil() {
        Country brazil = countryService.getCountryByName("brazil");
    
        List<Holiday> holidays = service.getUpcomingHolidays(brazil);

        assertNotNull(holidays);
        assertFalse(holidays.isEmpty(), "Lista de feriados não deveria estar vazia");

        Holiday first = holidays.get(0);

        assertNotNull(first.getDate());
        assertNotNull(first.getName());
        assertNotNull(first.getLocalName());
    }

    @Test
    void shouldContainChristmasDayInBrazilianHolidays() {
        Country brazil = countryService.getCountryByName("brazil");

        List<Holiday> holidays = service.getUpcomingHolidays(brazil);

        assertNotNull(holidays);
        assertFalse(holidays.isEmpty(), "Lista de feriados não está vazia");

        // Exemplo: verificar Natal
        boolean hasChristmas = holidays.stream().anyMatch(h ->
                h.getName().equalsIgnoreCase("Christmas Day") &&
                h.getLocalName().equalsIgnoreCase("Natal")
        );

        assertTrue(hasChristmas, "Deveria conter o feriado de Natal");

        // // Exemplo: verificar Ano Novo
        // boolean hasNewYear = holidays.stream().anyMatch(h ->
        //         h.getLocalName().equalsIgnoreCase("Ano Novo")
        // );

        // assertTrue(hasNewYear, "Deveria conter Ano Novo");
    }

    @Test
    void shouldHaveCorrectDateForChristmas() {
        Country brazil = countryService.getCountryByName("brazil");

        List<Holiday> holidays = service.getUpcomingHolidays(brazil);

        Holiday christmas = holidays.stream()
                .filter(h -> h.getLocalName().equalsIgnoreCase("Natal"))
                .findFirst()
                .orElseThrow();

        assertEquals(LocalDate.of(2026, 12, 25), christmas.getDate());
    }

    @Test
    void shouldHaveSortedDates() {
        Country brazil = countryService.getCountryByName("brazil");

        List<Holiday> holidays = service.getUpcomingHolidays(brazil);

        assertTrue(holidays.size() >= 5, "Deveria ter pelo menos 5 feriados");

        List<LocalDate> dates = holidays.stream()
                .map(Holiday::getDate)
                .toList();

        assertEquals(dates.stream().sorted().toList(), dates, "Datas devem estar ordenadas");
    }
}