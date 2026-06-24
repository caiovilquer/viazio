package br.usp.lab.oo.planejador_feriado.country.client;

import br.usp.lab.oo.planejador_feriado.country.dto.CountryDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StaticCountryClientTest {

    private final StaticCountryClient client = new StaticCountryClient(new ObjectMapper());

    @Test
    void findsCountryByCodeCaseInsensitively() {
        List<CountryDTO> upper = client.getCountryByCode("BR");
        List<CountryDTO> lower = client.getCountryByCode("br");

        assertEquals(1, upper.size());
        assertEquals("Brazil", upper.get(0).name().common());
        assertEquals(1, lower.size());
        assertEquals("BR", lower.get(0).isoCode());
    }

    @Test
    void exposesCompleteBundledCatalog() {
        List<CountryDTO> countries = client.getAllCountries();

        assertTrue(countries.size() >= 240);
        assertTrue(countries.stream().anyMatch(country -> "BR".equals(country.isoCode())));
        assertTrue(countries.stream().anyMatch(country -> "JP".equals(country.isoCode())));
    }

    @Test
    void bundledDataHasCoordinatesAndCurrency() {
        CountryDTO brazil = client.getCountryByCode("BR").get(0);

        assertFalse(brazil.latlng().isEmpty());
        assertTrue(brazil.currencies().containsKey("BRL"));
    }

    @Test
    void findsCountryByName() {
        List<CountryDTO> result = client.getCountryByName("japan");

        assertFalse(result.isEmpty());
        assertEquals("JP", result.get(0).isoCode());
    }

    @Test
    void findsCountriesByRegion() {
        List<CountryDTO> europe = client.getCountriesByRegion("Europe");

        assertFalse(europe.isEmpty());
        assertTrue(europe.stream().allMatch(c -> "Europe".equalsIgnoreCase(c.region())));
    }

    @Test
    void returnsEmptyForUnknownCode() {
        assertTrue(client.getCountryByCode("ZZ").isEmpty());
    }
}
