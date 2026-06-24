package br.usp.lab.oo.planejador_feriado.demographics.model;

/** População total de um país no ano mais recente disponível no Banco Mundial. */
public record Demographics(String isoCode, long population, String year) {
}
