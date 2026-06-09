package br.usp.lab.oo.planejador_feriado;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import br.usp.lab.oo.planejador_feriado.country.model.Country;
import br.usp.lab.oo.planejador_feriado.country.service.CountryService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Tag("integration")
class CountryServiceIntegrationTest {

    @Autowired
    private CountryService service;

    @Test
    void shouldReturnBrazilByCode() {
        Country country = service.getCountryByCode("br");

        assertNotNull(country);
        assertEquals("Brazil", country.getName());
        assertEquals("BR", country.getIsoCode());
        assertEquals("Americas", country.getRegion());
    }
}
