package br.usp.lab.oo.planejador_feriado.meta.dto;

public record ApiLimits(
        int recommendationWindowDays,
        int bestWindowsPeriodDays,
        int maximumResults,
        int maximumExplicitCandidates,
        int maximumRegionCandidates,
        int maximumTravelers
) {
}
