package br.usp.lab.oo.planejador_feriado.recommendation.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Resposta do endpoint de melhores janelas: os feriadões/pontes do período, ranqueados
 * por qualidade, cada um opcionalmente com seus melhores destinos.
 */
public record BestWindowsResponse(
        LocalDate from,
        LocalDate to,
        String profile,
        List<WindowSuggestion> windows
) {
}
