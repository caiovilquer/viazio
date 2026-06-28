package br.usp.lab.oo.planejador_feriado.cost.model;

/**
 * Nível de preços de um país relativo aos EUA, calculado com os indicadores do Banco
 * Mundial de PPP de consumo e câmbio oficial. É um proxy macroeconômico transparente,
 * não uma cotação de gastos turísticos em tempo real.
 *
 * @param countryCode      código ISO do país
 * @param priceLevelRatio  razão de nível de preços (PPP / câmbio de mercado)
 * @param year             ano de referência do dado
 */
public record CostOfLiving(
  String countryCode,
  double priceLevelRatio,
  String year
) {}
