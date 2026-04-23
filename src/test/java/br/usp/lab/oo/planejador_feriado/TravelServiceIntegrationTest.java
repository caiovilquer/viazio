package br.usp.lab.oo.planejador_feriado;

import br.usp.lab.oo.planejador_feriado.holiday.model.Holiday;
import br.usp.lab.oo.planejador_feriado.travel.model.TravelOverview;
import br.usp.lab.oo.planejador_feriado.travel.service.TravelService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TravelServiceIntegrationTest {

    @Autowired
    private TravelService travelService;

    @Test
    void shouldBuildOverviewForBrazilWithBrlAndNoExchange() {
        TravelOverview overview = travelService.getOverviewByCountryCode("br");

        assertNotNull(overview);
        assertNotNull(overview.country());
        assertEquals("BR", overview.country().getIsoCode());
        assertEquals("Brazil", overview.country().getName());

        List<Holiday> holidays = overview.upcomingHolidays();
        assertNotNull(holidays);
        assertFalse(holidays.isEmpty());

        assertNull(overview.exchangeToBrl(), "BRL destination should not fetch USD-BRL style quote");
    }
}
