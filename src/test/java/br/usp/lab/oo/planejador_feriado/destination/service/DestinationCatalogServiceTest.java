package br.usp.lab.oo.planejador_feriado.destination.service;

import br.usp.lab.oo.planejador_feriado.destination.client.StaticDestinationCatalog;
import br.usp.lab.oo.planejador_feriado.destination.model.DestinationCity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DestinationCatalogServiceTest {

    @Test
    void findsCityIgnoringCaseAndDiacritics() {
        StaticDestinationCatalog catalog = mock(StaticDestinationCatalog.class);
        when(catalog.findByCountry("BR")).thenReturn(List.of(
                new DestinationCity("BR", "Brasília", -15.79, -47.88, List.of(-3.0), true)));
        DestinationCatalogService service = new DestinationCatalogService(catalog);

        assertEquals("Brasília", service.findCity("BR", "brasilia").orElseThrow().name());
        assertEquals("Brasília", service.findCity("BR", "BRASÍLIA").orElseThrow().name());
    }
}
