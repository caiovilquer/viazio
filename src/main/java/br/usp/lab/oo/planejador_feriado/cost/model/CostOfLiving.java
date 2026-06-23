package br.usp.lab.oo.planejador_feriado.cost.model;

/**
 * Nível de preços de um país relativo aos EUA, a partir do indicador do Banco Mundial
 * {@code PA.NUS.PPPC.RF} (razão entre o fator de conversão PPP e a taxa de câmbio de
 * mercado). Valores abaixo de 1 indicam preços mais baratos que os americanos; é um
 * proxy melhor de "custo real de viagem" do que o câmbio nominal.
 *
 * @param countryCode      código ISO do país
 * @param priceLevelRatio  razão de nível de preços (PPP / câmbio de mercado)
 * @param year             ano de referência do dado
 */
public record CostOfLiving(String countryCode, double priceLevelRatio, String year) {
}
