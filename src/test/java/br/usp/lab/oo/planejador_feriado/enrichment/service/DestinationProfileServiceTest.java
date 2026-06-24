package br.usp.lab.oo.planejador_feriado.enrichment.service;

import br.usp.lab.oo.planejador_feriado.country.model.Country;
import br.usp.lab.oo.planejador_feriado.demographics.model.Demographics;
import br.usp.lab.oo.planejador_feriado.demographics.service.DemographicsService;
import br.usp.lab.oo.planejador_feriado.enrichment.model.DestinationProfile;
import br.usp.lab.oo.planejador_feriado.enrichment.model.WikiSummary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DestinationProfileServiceTest {

    @Mock
    private DemographicsService demographicsService;

    @Mock
    private WikipediaService wikipediaService;

    private Country brazil() {
        return new Country("Brazil", "Brasil", "BR", "Americas", "South America",
                List.of("Brasília"), List.of("Portuguese"), List.of("BRL"), List.of("UTC-03:00"), -10.0, -55.0);
    }

    @Test
    void buildsCompleteProfileWhenBothSourcesRespond() {
        when(demographicsService.getPopulation("BR"))
                .thenReturn(Optional.of(new Demographics("BR", 215_000_000L, "2023")));
        when(wikipediaService.getCountrySummary("Brasil", "Brazil"))
                .thenReturn(Optional.of(new WikiSummary(
                        "país na América do Sul", "O Brasil é...", "https://img.example/br.jpg",
                        "https://pt.wikipedia.org/wiki/Brasil")));

        DestinationProfileService service = new DestinationProfileService(demographicsService, wikipediaService);
        DestinationProfile profile = service.buildProfile(brazil());

        assertEquals("🇧🇷", profile.flagEmoji());
        assertEquals(215_000_000L, profile.population());
        assertEquals("2023", profile.populationYear());
        assertEquals("país na América do Sul", profile.description());
        assertEquals("O Brasil é...", profile.extract());
        assertEquals("https://img.example/br.jpg", profile.imageUrl());
        assertEquals("https://pt.wikipedia.org/wiki/Brasil", profile.wikipediaUrl());
    }

    @Test
    void keepsFlagAndWikiDataWhenPopulationLookupFails() {
        when(demographicsService.getPopulation("BR")).thenThrow(new RuntimeException("World Bank offline"));
        when(wikipediaService.getCountrySummary("Brasil", "Brazil"))
                .thenReturn(Optional.of(new WikiSummary("desc", "extract", null, null)));

        DestinationProfileService service = new DestinationProfileService(demographicsService, wikipediaService);
        DestinationProfile profile = service.buildProfile(brazil());

        assertEquals("🇧🇷", profile.flagEmoji());
        assertNull(profile.population());
        assertEquals("desc", profile.description());
    }

    @Test
    void keepsFlagAndPopulationWhenWikipediaLookupFails() {
        when(demographicsService.getPopulation("BR"))
                .thenReturn(Optional.of(new Demographics("BR", 215_000_000L, "2023")));
        when(wikipediaService.getCountrySummary("Brasil", "Brazil"))
                .thenThrow(new RuntimeException("Wikipedia offline"));

        DestinationProfileService service = new DestinationProfileService(demographicsService, wikipediaService);
        DestinationProfile profile = service.buildProfile(brazil());

        assertEquals("🇧🇷", profile.flagEmoji());
        assertEquals(215_000_000L, profile.population());
        assertNull(profile.description());
        assertNull(profile.imageUrl());
    }

    @Test
    void everythingNullWhenBothSourcesUnavailable() {
        when(demographicsService.getPopulation("BR")).thenReturn(Optional.empty());
        when(wikipediaService.getCountrySummary("Brasil", "Brazil")).thenReturn(Optional.empty());

        DestinationProfileService service = new DestinationProfileService(demographicsService, wikipediaService);
        DestinationProfile profile = service.buildProfile(brazil());

        assertEquals("🇧🇷", profile.flagEmoji());
        assertNull(profile.population());
        assertNull(profile.description());
        assertNull(profile.imageUrl());
        assertNull(profile.wikipediaUrl());
    }
}
