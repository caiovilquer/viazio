package br.usp.lab.oo.planejador_feriado.meta.service;

import br.usp.lab.oo.planejador_feriado.country.model.Country;
import br.usp.lab.oo.planejador_feriado.country.service.CountryService;
import br.usp.lab.oo.planejador_feriado.destination.model.DestinationCity;
import br.usp.lab.oo.planejador_feriado.destination.service.DestinationCatalogService;
import br.usp.lab.oo.planejador_feriado.meta.dto.ApiMetaResponse;
import br.usp.lab.oo.planejador_feriado.recommendation.config.ScoringProperties;
import br.usp.lab.oo.planejador_feriado.recommendation.weight.WeightResolver;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApiMetaServiceTest {

    @Test
    void exposesFrontendCatalogsProfilesLimitsAndCapabilities() {
        CountryService countryService = mock(CountryService.class);
        DestinationCatalogService destinationService = mock(DestinationCatalogService.class);
        Country brazil = new Country(
                "Brazil", "Brasil", "BR", "Americas", "South America",
                List.of("Brasília"), List.of("Portuguese"), List.of("BRL"), List.of("UTC-03:00"),
                -10.0, -55.0);
        when(countryService.getAllTravelEligibleCountries()).thenReturn(List.of(brazil));
        when(destinationService.getCities("BR")).thenReturn(List.of(
                new DestinationCity("BR", "Brasília", -15.79, -47.88, List.of(-3.0), true)));

        WeightResolver resolver = new WeightResolver(new ScoringProperties(
                Map.of("weather", 0.30, "cost", 0.30, "distance", 0.25, "festivities", 0.15),
                Map.of("economico", Map.of("cost", 0.60, "distance", 0.40))));
        ApiMetaService service = new ApiMetaService(countryService, destinationService, resolver);

        ApiMetaResponse response = service.getMetadata();

        assertEquals("v1", response.apiVersion());
        assertEquals(4, response.criteria().size());
        assertEquals("economico", response.profiles().get(0).key());
        assertEquals("BR", response.countries().get(0).code());
        assertEquals("Brasília", response.countries().get(0).defaultCity());
        assertEquals(92, response.limits().recommendationWindowDays());
        assertTrue(response.capabilities().get("groundBudgetFilter"));
        assertFalse(response.capabilities().get("liveCommercialPrices"));
    }

    @Test
    void cachesImmutableMetadata() {
        CountryService countryService = mock(CountryService.class);
        DestinationCatalogService destinationService = mock(DestinationCatalogService.class);
        when(countryService.getAllTravelEligibleCountries()).thenReturn(List.of());
        WeightResolver resolver = new WeightResolver(new ScoringProperties(Map.of(), Map.of()));
        ApiMetaService service = new ApiMetaService(countryService, destinationService, resolver);

        assertSame(service.getMetadata(), service.getMetadata());
    }
}
