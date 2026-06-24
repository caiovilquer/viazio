package br.usp.lab.oo.planejador_feriado.enrichment.service;

import br.usp.lab.oo.planejador_feriado.enrichment.client.WikipediaClient;
import br.usp.lab.oo.planejador_feriado.enrichment.dto.WikipediaSummaryDTO;
import br.usp.lab.oo.planejador_feriado.enrichment.model.WikiSummary;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Busca um resumo de página da Wikipédia para um país: tenta primeiro em português
 * (consistente com o restante da experiência do produto) e cai para o inglês quando
 * não há artigo em pt.wikipedia.org com aquele título.
 */
@Service
public class WikipediaService {

    private static final String PORTUGUESE = "pt";
    private static final String ENGLISH = "en";

    private final WikipediaClient client;

    public WikipediaService(WikipediaClient client) {
        this.client = client;
    }

    public Optional<WikiSummary> getCountrySummary(String portugueseName, String englishName) {
        WikipediaSummaryDTO summary = null;
        if (portugueseName != null && !portugueseName.isBlank()) {
            summary = client.getSummary(PORTUGUESE, portugueseName);
        }
        if (summary == null && englishName != null && !englishName.isBlank()) {
            summary = client.getSummary(ENGLISH, englishName);
        }
        return Optional.ofNullable(summary).map(this::toModel);
    }

    private WikiSummary toModel(WikipediaSummaryDTO dto) {
        String imageUrl = dto.originalimage() != null ? dto.originalimage().source()
                : dto.thumbnail() != null ? dto.thumbnail().source() : null;
        String pageUrl = dto.contentUrls() != null && dto.contentUrls().desktop() != null
                ? dto.contentUrls().desktop().page() : null;
        return new WikiSummary(dto.description(), dto.extract(), imageUrl, pageUrl);
    }
}
