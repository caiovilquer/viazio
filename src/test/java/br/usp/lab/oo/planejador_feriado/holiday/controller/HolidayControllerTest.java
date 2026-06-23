package br.usp.lab.oo.planejador_feriado.holiday.controller;

import br.usp.lab.oo.planejador_feriado.common.exception.ResourceNotFoundException;
import br.usp.lab.oo.planejador_feriado.country.model.Country;
import br.usp.lab.oo.planejador_feriado.country.service.CountryService;
import br.usp.lab.oo.planejador_feriado.holiday.model.Holiday;
import br.usp.lab.oo.planejador_feriado.holiday.service.HolidayService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HolidayController.class)
class HolidayControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CountryService countryService;

    @MockitoBean
    private HolidayService holidayService;

    @Test
    void shouldReturnUpcomingHolidays() throws Exception {
        Country brazil = new Country("Brazil", "BR", "Americas", "South America",
                List.of("Brasília"), List.of("Portuguese"), List.of("BRL"), List.of("UTC-03:00"));
        Holiday holiday = new Holiday(LocalDate.of(2026, 12, 25), "Christmas Day", "Natal", List.of("Public"));

        when(countryService.getCountryByCode("BR")).thenReturn(brazil);
        when(holidayService.getUpcomingHolidays(any(Country.class))).thenReturn(List.of(holiday));

        mockMvc.perform(get("/api/v1/holidays/BR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Christmas Day"))
                .andExpect(jsonPath("$[0].date").value("2026-12-25"));
    }

    @Test
    void shouldReturn404WhenCountryNotFound() throws Exception {
        when(countryService.getCountryByCode("ZZ"))
                .thenThrow(new ResourceNotFoundException("Country not found: ZZ"));

        mockMvc.perform(get("/api/v1/holidays/ZZ"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/api/v1/holidays/ZZ"));
    }
}
