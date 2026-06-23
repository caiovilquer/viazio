package br.usp.lab.oo.planejador_feriado.recommendation.weight;

import br.usp.lab.oo.planejador_feriado.recommendation.model.Criterion;

import java.util.Map;

/**
 * Pesos efetivamente aplicados numa busca, já normalizados (somam 1), com o nome do
 * perfil resolvido para ecoar de volta na resposta (transparência para a UI).
 */
public record ResolvedWeights(String profileName, Map<Criterion, Double> weights) {

    public double weightOf(Criterion criterion) {
        return weights.getOrDefault(criterion, 0.0);
    }

    /** Pesos por chave de critério (ex.: "weather") para serializar na resposta. */
    public Map<String, Double> asKeyedMap() {
        return weights.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        e -> e.getKey().key(),
                        Map.Entry::getValue,
                        (a, b) -> a,
                        java.util.LinkedHashMap::new));
    }
}
