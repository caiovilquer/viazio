package br.usp.lab.oo.planejador_feriado.common.geo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeoCalculatorTest {

    @Test
    void distanceToSamePointIsZero() {
        assertEquals(0.0, GeoCalculator.haversineKm(-23.5, -46.6, -23.5, -46.6), 0.0001);
    }

    @Test
    void estimatesKnownDistanceWithinTolerance() {
        // São Paulo (-23.55, -46.63) → Buenos Aires (-34.60, -58.38) ≈ 1680 km
        double km = GeoCalculator.haversineKm(-23.55, -46.63, -34.60, -58.38);
        assertTrue(km > 1500 && km < 1850, "expected ~1680 km, got " + km);
    }

    @Test
    void farDestinationIsLargerThanNearOne() {
        double near = GeoCalculator.haversineKm(-15.0, -47.0, -34.6, -58.4);   // Brasília → Buenos Aires
        double far = GeoCalculator.haversineKm(-15.0, -47.0, 35.6, 139.7);     // Brasília → Tóquio
        assertTrue(far > near);
    }
}
