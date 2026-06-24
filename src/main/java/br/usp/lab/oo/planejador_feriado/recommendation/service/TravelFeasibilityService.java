package br.usp.lab.oo.planejador_feriado.recommendation.service;

import br.usp.lab.oo.planejador_feriado.cost.model.CostOfLiving;
import br.usp.lab.oo.planejador_feriado.destination.model.DestinationCity;
import br.usp.lab.oo.planejador_feriado.recommendation.config.TravelEstimateProperties;
import br.usp.lab.oo.planejador_feriado.recommendation.model.GroundCostEstimate;
import br.usp.lab.oo.planejador_feriado.recommendation.model.TravelEffort;
import br.usp.lab.oo.planejador_feriado.recommendation.model.TripFeasibility;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Produz estimativas transparentes quando não há cotação comercial de voo/hotel.
 * Nunca apresenta o resultado como preço real: premissas e itens não incluídos
 * acompanham a resposta.
 */
@Service
public class TravelFeasibilityService {

    private final TravelEstimateProperties properties;

    public TravelFeasibilityService(TravelEstimateProperties properties) {
        this.properties = properties;
    }

    public TripFeasibility build(
            DestinationCity origin,
            DestinationCity destination,
            double distanceKm,
            CostOfLiving originCost,
            CostOfLiving destinationCost,
            int days,
            int travelers) {
        if (origin == null || destination == null) {
            throw new IllegalArgumentException("Origem e destino são obrigatórios");
        }
        if (!Double.isFinite(distanceKm) || distanceKm < 0.0) {
            throw new IllegalArgumentException("Distância deve ser um valor não negativo");
        }
        if (days < 1 || travelers < 1) {
            throw new IllegalArgumentException("Dias e viajantes devem ser positivos");
        }
        return new TripFeasibility(
                destination,
                travelEffort(origin, destination, distanceKm),
                groundCost(originCost, destinationCost, days, travelers),
                List.of(
                        "passagens aéreas",
                        "bagagem e taxas aeroportuárias",
                        "seguro-viagem",
                        "visto, vacinas e demais requisitos de entrada"));
    }

    private TravelEffort travelEffort(
            DestinationCity origin,
            DestinationCity destination,
            double distanceKm) {
        double minHours = Math.max(1.5, distanceKm / 900.0 + 1.0);
        double maxHours = Math.max(minHours + 0.5, distanceKm / 700.0 + 3.0);
        Double originOffset = firstOffset(origin);
        Double destinationOffset = firstOffset(destination);
        Double difference = originOffset != null && destinationOffset != null
                ? Math.abs(destinationOffset - originOffset)
                : null;
        return new TravelEffort(
                round(distanceKm),
                round(minHours),
                round(maxHours),
                originOffset,
                destinationOffset,
                difference,
                classify(distanceKm),
                true);
    }

    private GroundCostEstimate groundCost(
            CostOfLiving origin,
            CostOfLiving destination,
            int days,
            int travelers) {
        if (origin == null
                || destination == null
                || !Double.isFinite(origin.priceLevelRatio())
                || !Double.isFinite(destination.priceLevelRatio())
                || origin.priceLevelRatio() <= 0.0
                || destination.priceLevelRatio() <= 0.0) {
            return null;
        }
        double relative = destination.priceLevelRatio() / origin.priceLevelRatio();
        double daily = properties.baselineDailyGroundCostBrlOrDefault() * relative;
        return new GroundCostEstimate(
                "BRL",
                round(daily),
                round(daily * days * travelers),
                travelers,
                days,
                round(relative),
                destination.year(),
                origin.year(),
                "LOW",
                "Estimativa terrestre por PPP; base de R$ "
                        + round(properties.baselineDailyGroundCostBrlOrDefault())
                        + " por pessoa/dia na origem");
    }

    private Double firstOffset(DestinationCity city) {
        return city.utcOffsets().isEmpty() ? null : city.utcOffsets().get(0);
    }

    private String classify(double distanceKm) {
        if (distanceKm < 2500.0) {
            return "SHORT";
        }
        if (distanceKm < 7000.0) {
            return "MEDIUM";
        }
        return "LONG";
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
