package br.usp.lab.oo.planejador_feriado.recommendation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.recommendation.estimates")
public record TravelEstimateProperties(double baselineDailyGroundCostBrl) {

    public double baselineDailyGroundCostBrlOrDefault() {
        return baselineDailyGroundCostBrl > 0.0 ? baselineDailyGroundCostBrl : 350.0;
    }
}
