package br.usp.lab.oo.planejador_feriado.recommendation.metrics;

import br.usp.lab.oo.planejador_feriado.recommendation.dto.RecommendationResponse;
import br.usp.lab.oo.planejador_feriado.recommendation.model.OriginReference;
import br.usp.lab.oo.planejador_feriado.recommendation.model.SkippedCandidate;
import br.usp.lab.oo.planejador_feriado.recommendation.model.WindowAssessment;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RecommendationMetricsTest {

    @Test
    void recordsSuccessfulAndFailedSearchesWithoutHighCardinalityTags() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        RecommendationMetrics metrics = new RecommendationMetrics(registry);

        metrics.measure(() -> response(List.of(new SkippedCandidate("XX", "indisponível"))));
        metrics.recordResults(42, 10);
        assertThrows(IllegalStateException.class, () -> metrics.measure(() -> {
            throw new IllegalStateException("failure");
        }));

        assertEquals(1.0, registry.counter(
                "travel.recommendation.requests", "outcome", "success").count());
        assertEquals(1.0, registry.counter(
                "travel.recommendation.requests", "outcome", "error").count());
        assertEquals(1L, registry.get("travel.recommendation.duration")
                .tag("outcome", "success").timer().count());
        assertEquals(42.0, registry.get("travel.recommendation.candidates.evaluated")
                .summary().totalAmount());
    }

    private RecommendationResponse response(List<SkippedCandidate> skipped) {
        return new RecommendationResponse(
                LocalDate.of(2026, 9, 4),
                LocalDate.of(2026, 9, 7),
                Instant.parse("2026-06-23T00:00:00Z"),
                new OriginReference("BR", null, -15.79, -47.88, "Brasília"),
                "padrão",
                Map.of(),
                new WindowAssessment(50.0, 4, 2, 2, List.of(), "janela comum"),
                List.of(),
                skipped);
    }
}
