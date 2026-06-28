package br.usp.lab.oo.planejador_feriado.recommendation.model;

/**
 * Entrada do breakdown exibido na API: combina a nota normalizada do critério com o
 * peso aplicado e a contribuição efetiva no score final, além de rótulo e ícone
 * prontos para a UI. A soma das {@code contribution} dos critérios disponíveis é o
 * score final do destino.
 *
 * @param criterion     chave estável (ex.: "weather")
 * @param label         rótulo amigável (ex.: "Clima")
 * @param icon          ícone para a UI
 * @param available     se o dado estava disponível (senão não conta no score)
 * @param score         nota normalizada 0–100 do critério
 * @param weight        peso configurado (0–1) para este critério
 * @param contribution  pontos que este critério adicionou ao score final
 * @param justification explicação legível da nota
 */
public record ScoredCriterion(
  String criterion,
  String label,
  String icon,
  boolean available,
  double score,
  double weight,
  double contribution,
  String justification
) {}
