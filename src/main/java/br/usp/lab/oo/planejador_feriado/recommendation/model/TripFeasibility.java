package br.usp.lab.oo.planejador_feriado.recommendation.model;

import br.usp.lab.oo.planejador_feriado.destination.model.DestinationCity;
import java.util.List;

public record TripFeasibility(
  DestinationCity destination,
  TravelEffort travelEffort,
  GroundCostEstimate groundCost,
  List<String> notIncluded
) {
  public TripFeasibility {
    notIncluded = notIncluded != null ? List.copyOf(notIncluded) : List.of();
  }
}
