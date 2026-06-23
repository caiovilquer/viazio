package br.usp.lab.oo.planejador_feriado.country.controller;

import br.usp.lab.oo.planejador_feriado.common.exception.ExternalApiException;
import br.usp.lab.oo.planejador_feriado.common.exception.ResourceNotFoundException;
import br.usp.lab.oo.planejador_feriado.country.model.Country;
import br.usp.lab.oo.planejador_feriado.country.service.CountryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CountryController.class)
class CountryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CountryService countryService;

    private Country japan() {
        return new Country("Japan", "JP", "Asia", "Eastern Asia",
                List.of("Tokyo"), List.of("Japanese"), List.of("JPY"), List.of("UTC+09:00"));
    }

    @Test
    void shouldReturnCountryByCode() throws Exception {
        when(countryService.getCountryByCode("JP")).thenReturn(japan());

        mockMvc.perform(get("/api/v1/countries/JP"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Japan"))
                .andExpect(jsonPath("$.isoCode").value("JP"));
    }

    @Test
    void shouldReturn404WhenCountryNotFound() throws Exception {
        when(countryService.getCountryByCode("ZZ"))
                .thenThrow(new ResourceNotFoundException("Country not found: ZZ"));

        mockMvc.perform(get("/api/v1/countries/ZZ"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Country not found: ZZ"))
                .andExpect(jsonPath("$.path").value("/api/v1/countries/ZZ"));
    }

    @Test
    void shouldReturn502WhenUpstreamFails() throws Exception {
        when(countryService.getCountryByCode("JP"))
                .thenThrow(new ExternalApiException("Falha ao consultar serviço de países", new RuntimeException()));

        mockMvc.perform(get("/api/v1/countries/JP"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502));
    }

    @Test
    void shouldSearchCountryByName() throws Exception {
        when(countryService.getCountryByName("japan")).thenReturn(japan());

        mockMvc.perform(get("/api/v1/countries/search").param("name", "japan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Japan"));
    }
}
