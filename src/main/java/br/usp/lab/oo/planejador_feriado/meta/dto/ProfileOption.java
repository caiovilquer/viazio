package br.usp.lab.oo.planejador_feriado.meta.dto;

import java.util.Map;

public record ProfileOption(
        String key,
        String label,
        Map<String, Double> weights
) {
    public ProfileOption {
        weights = Map.copyOf(weights);
    }
}
