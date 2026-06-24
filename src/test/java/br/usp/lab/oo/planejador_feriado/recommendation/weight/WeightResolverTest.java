package br.usp.lab.oo.planejador_feriado.recommendation.weight;

import br.usp.lab.oo.planejador_feriado.recommendation.config.ScoringProperties;
import br.usp.lab.oo.planejador_feriado.recommendation.model.Criterion;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeightResolverTest {

    private final WeightResolver resolver = new WeightResolver(new ScoringProperties(
            Map.of(
                    "weather", 0.30, "cost", 0.30, "distance", 0.25, "festivities", 0.15
            ),
            Map.of(
                    "economico", Map.of(
                            "cost", 0.55, "distance", 0.25, "weather", 0.15, "festivities", 0.05),
                    "clima-perfeito", Map.of(
                            "weather", 0.60, "cost", 0.18, "festivities", 0.12, "distance", 0.10)
            )
    ));

    @Test
    void defaultProfileUsesDefaultWeightsAndNormalizesToOne() {
        ResolvedWeights resolved = resolver.resolve(null, Map.of());

        assertEquals("padrão", resolved.profileName());
        assertEquals(1.0, totalWeight(resolved), 0.0001);
    }

    @Test
    void knownProfileIsApplied() {
        ResolvedWeights resolved = resolver.resolve("economico", Map.of());

        assertEquals("economico", resolved.profileName());
        assertTrue(resolved.weightOf(Criterion.COST) > resolved.weightOf(Criterion.DISTANCE));
        assertEquals(1.0, totalWeight(resolved), 0.0001);
    }

    @Test
    void overridesAdjustProfileAndAreNormalized() {
        ResolvedWeights resolved = resolver.resolve("economico", Map.of(Criterion.WEATHER, 0.5));

        assertEquals("economico (ajustado)", resolved.profileName());
        assertEquals(1.0, totalWeight(resolved), 0.0001);
        assertTrue(resolved.weightOf(Criterion.WEATHER) > 0.2);
    }

    @Test
    void overridesWithoutProfileAreCustom() {
        ResolvedWeights resolved = resolver.resolve(null, Map.of(Criterion.DISTANCE, 0.9));

        assertEquals("personalizado", resolved.profileName());
        assertEquals(1.0, totalWeight(resolved), 0.0001);
    }

    @Test
    void exposesKnownProfilesSorted() {
        assertTrue(resolver.isKnownProfile("economico"));
        assertFalse(resolver.isKnownProfile("inexistente"));
        assertEquals(List.of("clima-perfeito", "economico"), resolver.availableProfiles());
    }

    private double totalWeight(ResolvedWeights resolved) {
        return resolved.weights().values().stream().mapToDouble(Double::doubleValue).sum();
    }
}
