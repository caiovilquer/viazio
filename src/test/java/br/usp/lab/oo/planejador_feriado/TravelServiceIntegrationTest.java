package br.usp.lab.oo.planejador_feriado;

import br.usp.lab.oo.planejador_feriado.travel.model.TravelOverview;
import br.usp.lab.oo.planejador_feriado.travel.service.TravelService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@Tag("integration")
class TravelServiceIntegrationTest {

    @Autowired
    private TravelService travelService;

    @Test
    void shouldBuildOverviewForBrazilWithBrlAndNoExchange() {
        TravelOverview overview = travelService.getOverviewByCountryCode("br");

        assertNotNull(overview);
        assertEquals("BR", overview.country().getIsoCode());
        assertNotNull(overview.upcomingHolidays());
        assertNull(overview.exchangeToBrl());
    }
}
