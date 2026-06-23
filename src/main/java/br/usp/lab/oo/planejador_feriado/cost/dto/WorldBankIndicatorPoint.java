package br.usp.lab.oo.planejador_feriado.cost.dto;

/**
 * Um ponto da série de um indicador do Banco Mundial (ano + valor), já extraído do
 * formato de array heterogêneo retornado pela API.
 */
public record WorldBankIndicatorPoint(String year, Double value) {
}
