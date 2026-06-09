package br.usp.lab.oo.planejador_feriado.recommendation.model;

public record ScoreEntry(
        String criterion,
        double points,
        double maxPoints,
        String justification
) {
}
