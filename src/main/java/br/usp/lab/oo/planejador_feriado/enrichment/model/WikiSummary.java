package br.usp.lab.oo.planejador_feriado.enrichment.model;

/** Resumo de um destino extraído da Wikipédia: descrição curta, imagem e link da página. */
public record WikiSummary(String description, String extract, String imageUrl, String pageUrl) {
}
