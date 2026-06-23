package br.usp.lab.oo.planejador_feriado.recommendation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * Pesos do motor de recomendação, externalizados em {@code application.yml}.
 *
 * @param defaultWeights peso por critério usado quando nenhum perfil é escolhido
 * @param profiles       presets nomeados (ex.: "economico") → peso por critério
 */
@ConfigurationProperties(prefix = "app.recommendation")
public record ScoringProperties(
        Map<String, Double> defaultWeights,
        Map<String, Map<String, Double>> profiles
) {

    public Map<String, Double> defaultWeights() {
        return defaultWeights != null ? defaultWeights : Map.of();
    }

    public Map<String, Map<String, Double>> profiles() {
        return profiles != null ? profiles : Map.of();
    }
}
