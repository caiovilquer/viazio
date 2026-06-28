package br.usp.lab.oo.planejador_feriado.recommendation.model;

import java.util.Locale;
import java.util.Optional;

/**
 * Catálogo dos critérios de avaliação de um destino. Centraliza chave (estável para
 * a API e para os pesos), rótulo amigável e ícone — consumidos diretamente pelo
 * frontend para montar a legenda e o breakdown sem precisar de um de/para próprio.
 */
public enum Criterion {
  WEATHER("Clima", "☀️"),
  COST("Custo de vida", "💰"),
  DISTANCE("Distância", "✈️"),
  FESTIVITIES("Festividades no destino", "🎊");

  private final String label;
  private final String icon;

  Criterion(String label, String icon) {
    this.label = label;
    this.icon = icon;
  }

  /** Chave estável usada na API e na configuração de pesos (ex.: "festivities"). */
  public String key() {
    return name().toLowerCase(Locale.ROOT);
  }

  public String label() {
    return label;
  }

  public String icon() {
    return icon;
  }

  public static Optional<Criterion> fromKey(String key) {
    if (key == null) {
      return Optional.empty();
    }
    String normalized = key.trim().toUpperCase(Locale.ROOT);
    for (Criterion criterion : values()) {
      if (criterion.name().equals(normalized)) {
        return Optional.of(criterion);
      }
    }
    return Optional.empty();
  }
}
