package br.usp.lab.oo.planejador_feriado;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import br.usp.lab.oo.planejador_feriado.country.model.Country;
import br.usp.lab.oo.planejador_feriado.country.service.CountryService;
import br.usp.lab.oo.planejador_feriado.holiday.model.Holiday;
import br.usp.lab.oo.planejador_feriado.holiday.service.HolidayService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Tag("integration")
class HolidayServiceIntegrationTest {

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
    }
}
