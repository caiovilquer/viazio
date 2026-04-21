package br.usp.lab.oo.planejador_feriado;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import br.usp.lab.oo.planejador_feriado.country.model.Country;
import br.usp.lab.oo.planejador_feriado.country.service.CountryService;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CountryServiceIntegrationTest {

    @Autowired
    private CountryService service;

    @Test
    void shouldReturnBrazilByName() {
        Country country = service.getCountryByName("brazil");

        assertNotNull(country);
        assertEquals("Brazil", country.getName());
        assertEquals("BR", country.getIsoCode());
        assertEquals("Americas", country.getRegion());
        assertEquals("South America", country.getSubregion());

        assertEquals(1, country.getCapitals().size());
        assertTrue(country.getCapitals().contains("Brasília"));

        assertEquals(1, country.getLanguages().size());
        assertTrue(country.getLanguages().contains("Portuguese"));

        assertEquals(1, country.getCurrencies().size());
        assertTrue(country.getCurrencies().contains("BRL"));

        assertFalse(country.getTimezones().isEmpty());
        assertTrue(country.getTimezones().contains("UTC-03:00"));
    }

    @Test
    void shouldReturnBrazilByCode() {
        Country country = service.getCountryByCode("br");

        assertNotNull(country);
        assertEquals("Brazil", country.getName());
        assertEquals("BR", country.getIsoCode());
        assertEquals("Americas", country.getRegion());
        assertEquals("South America", country.getSubregion());

        assertEquals(1, country.getCapitals().size());
        assertTrue(country.getCapitals().contains("Brasília"));

        assertEquals(1, country.getLanguages().size());
        assertTrue(country.getLanguages().contains("Portuguese"));

        assertEquals(1, country.getCurrencies().size());
        assertTrue(country.getCurrencies().contains("BRL"));

        assertFalse(country.getTimezones().isEmpty());
        assertTrue(country.getTimezones().contains("UTC-03:00"));
    }
}