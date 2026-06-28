package br.usp.lab.oo.planejador_feriado.enrichment.service;

import br.usp.lab.oo.planejador_feriado.enrichment.client.WikipediaClient;
import br.usp.lab.oo.planejador_feriado.enrichment.dto.WikipediaSummaryDTO;
import br.usp.lab.oo.planejador_feriado.enrichment.model.WikiSummary;
import java.util.Optional;
import org.springframework.stereotype.Service;

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

  public Optional<WikiSummary> getCountrySummary(
    String portugueseName,
    String englishName
  ) {
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
    String imageUrl =
      dto.originalimage() != null
        ? dto.originalimage().source()
        : dto.thumbnail() != null
          ? dto.thumbnail().source()
          : null;
    String pageUrl =
      dto.contentUrls() != null && dto.contentUrls().desktop() != null
        ? dto.contentUrls().desktop().page()
        : null;
    return new WikiSummary(
      dto.description(),
      cleanExtract(dto.extract()),
      imageUrl,
      pageUrl
    );
  }

  /**
   * Remove artefatos comuns do endpoint de texto plano da Wikipédia:
   * <ul>
   *   <li>Parênteses vazios/espaçados — templates de pronúncia (ex.: {@code ()}, {@code ( )})
   *       que não são renderizados no extract.</li>
   *   <li>Parêntesis de fechamento órfão logo após o título do artigo (ex.: {@code Colômbia ) ,})
   *       que sobram quando o conteúdo interno é removido pelo parser.</li>
   * </ul>
   */
  private static String cleanExtract(String extract) {
    if (extract == null || extract.isBlank()) return extract;
    return extract
      .replaceAll("\\(\\s*\\)", "") // "()" ou "( )"
      .replaceAll("\\s*\\)\\s*([,;])", "$1") // " ) ," → ","
      .replaceAll("^\\s*\\)\\s*", "") // ")" solto no início
      .replaceAll(" {2,}", " ") // espaços duplos
      .trim();
  }
}
