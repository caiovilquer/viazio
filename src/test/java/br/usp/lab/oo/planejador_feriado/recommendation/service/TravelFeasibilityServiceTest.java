package br.usp.lab.oo.planejador_feriado.recommendation.service;

import br.usp.lab.oo.planejador_feriado.cost.model.CostOfLiving;
import br.usp.lab.oo.planejador_feriado.destination.model.DestinationCity;
import br.usp.lab.oo.planejador_feriado.recommendation.config.TravelEstimateProperties;
import br.usp.lab.oo.planejador_feriado.recommendation.model.TripFeasibility;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TravelFeasibilityServiceTest {

    private final TravelFeasibilityService service =
            new TravelFeasibilityService(new TravelEstimateProperties(350.0));

    @Test
    void estimatesGroundCostAnchoredToBrazilReferenceAndExposesAssumptions() {
        TripFeasibility feasibility = service.build(
                city("BR", "Brasília", -3.0),
                city("JP", "Tokyo", 9.0),
                9_000.0,
                new CostOfLiving("BR", 0.4, "2024"),
                new CostOfLiving("JP", 0.6, "2024"),
                7,
                2);

        assertEquals(9_000.0, feasibility.travelEffort().distanceKm());
        assertEquals(11.0, feasibility.travelEffort().estimatedTravelHoursMin());
        assertEquals(15.9, feasibility.travelEffort().estimatedTravelHoursMax());
        assertEquals(12.0, feasibility.travelEffort().timeZoneDifferenceHours());
        assertEquals("LONG", feasibility.travelEffort().classification());
        assertTrue(feasibility.travelEffort().estimated());

        // base 350 BRL (um dia no Brasil) × (0.6 / 0.4) = 525
        assertEquals(525.0, feasibility.groundCost().estimatedDailyPerPerson());
        assertEquals(7_350.0, feasibility.groundCost().estimatedTotal());
        assertEquals(1.5, feasibility.groundCost().relativePriceLevel());
        assertEquals("LOW", feasibility.groundCost().confidence());
        assertTrue(feasibility.groundCost().assumption().contains("PPP"));
        assertTrue(feasibility.notIncluded().contains("passagens aéreas"));
    }

    @Test
    void groundCostDependsOnBrazilReferenceNotOnOriginCity() {
        // Mesmo destino e mesma referência (Brasil) → mesmo custo em BRL, ainda que a
        // viagem parta de cidades/origens diferentes. O custo absoluto de um dia no
        // destino é propriedade do destino, não de onde se parte.
        CostOfLiving brazilReference = new CostOfLiving("BR", 0.4, "2024");
        CostOfLiving japanCost = new CostOfLiving("JP", 0.6, "2024");

        TripFeasibility fromBrazil = service.build(
                city("BR", "Brasília", -3.0), city("JP", "Tokyo", 9.0), 9_000.0,
                brazilReference, japanCost, 7, 2);
        TripFeasibility fromBelize = service.build(
                city("BZ", "Belmopan", -6.0), city("JP", "Tokyo", 9.0), 14_000.0,
                brazilReference, japanCost, 7, 2);

        assertEquals(
                fromBrazil.groundCost().estimatedDailyPerPerson(),
                fromBelize.groundCost().estimatedDailyPerPerson());
        assertEquals("2024", fromBelize.groundCost().referenceDataYear());
    }

    @Test
    void omitsGroundCostWhenPppDataIsUnavailable() {
        TripFeasibility feasibility = service.build(
                city("BR", "Brasília", -3.0),
                city("AR", "Buenos Aires", -3.0),
                2_300.0,
                null,
                null,
                4,
                1);

        assertNull(feasibility.groundCost());
        assertEquals(0.0, feasibility.travelEffort().timeZoneDifferenceHours());
    }

    @Test
    void rejectsInvalidTripDimensions() {
        assertThrows(IllegalArgumentException.class, () -> service.build(
                city("BR", "Brasília", -3.0),
                city("AR", "Buenos Aires", -3.0),
                -1.0,
                null,
                null,
                0,
                0));
    }

    private DestinationCity city(String country, String name, double offset) {
        return new DestinationCity(country, name, 0.0, 0.0, List.of(offset), true);
    }
}
