package br.usp.lab.oo.planejador_feriado.recommendation.weight;

import br.usp.lab.oo.planejador_feriado.recommendation.config.ScoringProperties;
import br.usp.lab.oo.planejador_feriado.recommendation.model.Criterion;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
import org.springframework.stereotype.Component;

/**
 * Resolve os pesos efetivos de uma busca combinando: pesos padrão → perfil escolhido
 * → ajustes finos por critério. Ignora critérios desconhecidos, zera pesos negativos
 * e normaliza para somar 1, de modo que o score final seja sempre uma média ponderada
 * comparável entre destinos e janelas.
 */
@Component
public class WeightResolver {

  private final ScoringProperties properties;

  public WeightResolver(ScoringProperties properties) {
    this.properties = properties;
  }

  /** Nomes de perfis disponíveis (ordenados), para validação e para o endpoint de metadados. */
  public List<String> availableProfiles() {
    return new TreeSet<>(properties.profiles().keySet()).stream().toList();
  }

  public boolean isKnownProfile(String profile) {
    return (
      profile != null &&
      properties.profiles().containsKey(profile.toLowerCase(Locale.ROOT))
    );
  }

  public ResolvedWeights resolve(
    String profile,
    Map<Criterion, Double> overrides
  ) {
    String normalizedProfile =
      profile != null ? profile.trim().toLowerCase(Locale.ROOT) : null;

    Map<String, Double> base = properties.defaultWeights();
    boolean usingProfile =
      normalizedProfile != null &&
      properties.profiles().containsKey(normalizedProfile);
    if (usingProfile) {
      base = properties.profiles().get(normalizedProfile);
    }

    Map<Criterion, Double> weights = new EnumMap<>(Criterion.class);
    for (Criterion criterion : Criterion.values()) {
      weights.put(criterion, 0.0);
    }
    base.forEach((key, value) ->
      Criterion.fromKey(key).ifPresent(criterion ->
        weights.put(criterion, Math.max(0.0, value))
      )
    );

    boolean hasOverrides = overrides != null && !overrides.isEmpty();
    if (hasOverrides) {
      overrides.forEach((criterion, value) -> {
        if (criterion != null && value != null) {
          weights.put(criterion, Math.max(0.0, value));
        }
      });
    }

    normalize(weights);

    return new ResolvedWeights(
      resolveName(usingProfile, normalizedProfile, hasOverrides),
      weights
    );
  }

  private void normalize(Map<Criterion, Double> weights) {
    double sum = weights
      .values()
      .stream()
      .mapToDouble(Double::doubleValue)
      .sum();
    if (sum <= 0.0) {
      double equal = 1.0 / Criterion.values().length;
      weights.replaceAll((criterion, value) -> equal);
      return;
    }
    weights.replaceAll((criterion, value) -> value / sum);
  }

  private String resolveName(
    boolean usingProfile,
    String profile,
    boolean hasOverrides
  ) {
    if (usingProfile) {
      return hasOverrides ? profile + " (ajustado)" : profile;
    }
    return hasOverrides ? "personalizado" : "padrão";
  }
}
