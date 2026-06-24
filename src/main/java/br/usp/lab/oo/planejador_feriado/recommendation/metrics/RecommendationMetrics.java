package br.usp.lab.oo.planejador_feriado.recommendation.metrics;

import br.usp.lab.oo.planejador_feriado.recommendation.dto.RecommendationResponse;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.function.Supplier;

@Component
public class RecommendationMetrics {

    private final MeterRegistry registry;
    private final DistributionSummary evaluatedCandidates;
    private final DistributionSummary returnedRecommendations;

    public RecommendationMetrics(MeterRegistry registry) {
        this.registry = registry;
        this.evaluatedCandidates = DistributionSummary.builder("travel.recommendation.candidates.evaluated")
                .description("Quantidade de candidatos avaliados por busca")
                .register(registry);
        this.returnedRecommendations = DistributionSummary.builder("travel.recommendation.results.returned")
                .description("Quantidade de recomendações devolvidas por busca")
                .register(registry);
    }

    public RecommendationResponse measure(Supplier<RecommendationResponse> operation) {
        long startedAt = System.nanoTime();
        String outcome = "success";
        try {
            RecommendationResponse response = operation.get();
            registry.counter("travel.recommendation.requests", "outcome", outcome).increment();
            return response;
        } catch (RuntimeException exception) {
            outcome = "error";
            registry.counter("travel.recommendation.requests", "outcome", outcome).increment();
            throw exception;
        } finally {
            Timer.builder("travel.recommendation.duration")
                    .description("Tempo total para gerar uma resposta de recomendação")
                    .tag("outcome", outcome)
                    .register(registry)
                    .record(Duration.ofNanos(System.nanoTime() - startedAt));
        }
    }

    public void recordResults(int evaluated, int returned) {
        evaluatedCandidates.record(evaluated);
        returnedRecommendations.record(returned);
    }
}
