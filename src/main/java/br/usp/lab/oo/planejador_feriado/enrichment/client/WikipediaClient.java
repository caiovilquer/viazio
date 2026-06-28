package br.usp.lab.oo.planejador_feriado.enrichment.client;

import br.usp.lab.oo.planejador_feriado.enrichment.dto.WikipediaSummaryDTO;

/**
 * Abstrai o acesso ao endpoint REST de resumo de página da Wikipédia, permitindo
 * decorar a implementação real (ex.: com cache) sem que {@code WikipediaService}
 * conheça o detalhe.
 */
public interface WikipediaClient {
  /** Resumo da página {@code title} na Wikipédia em {@code languageCode}, ou {@code null} se não existir. */
  WikipediaSummaryDTO getSummary(String languageCode, String title);
}
