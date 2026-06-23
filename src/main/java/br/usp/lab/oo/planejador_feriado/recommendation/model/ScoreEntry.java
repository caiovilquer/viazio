package br.usp.lab.oo.planejador_feriado.recommendation.model;

/**
 * Resultado de uma {@code ScoringStrategy} para um critério: uma nota normalizada
 * de 0 a 100 (comparável entre critérios) e a justificativa legível.
 *
 * <p>Quando o dado necessário não está disponível (ex.: clima ou custo que a API
 * externa não retornou), {@code available} é {@code false} e o critério é excluído
 * da média ponderada — em vez de penalizar o destino com nota zero.</p>
 */
public record ScoreEntry(
        Criterion criterion,
        boolean available,
        double score,
        String justification
) {

    public static ScoreEntry of(Criterion criterion, double score, String justification) {
        double clamped = Math.max(0.0, Math.min(100.0, score));
        return new ScoreEntry(criterion, true, clamped, justification);
    }

    public static ScoreEntry unavailable(Criterion criterion, String reason) {
        return new ScoreEntry(criterion, false, 0.0, reason);
    }
}
