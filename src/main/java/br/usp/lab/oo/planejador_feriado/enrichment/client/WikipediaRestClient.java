package br.usp.lab.oo.planejador_feriado.enrichment.client;

import br.usp.lab.oo.planejador_feriado.common.config.ExternalApisProperties;
import br.usp.lab.oo.planejador_feriado.common.config.RestClientFactory;
import br.usp.lab.oo.planejador_feriado.enrichment.dto.WikipediaSummaryDTO;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.Locale;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

/**
 * Consome o endpoint REST {@code page/summary} da Wikipédia. A API usa um subdomínio
 * por idioma (ex.: {@code pt.wikipedia.org}), então o {@link RestClient} é construído
 * por chamada a partir do template configurado em {@code app.external-apis}.
 */
@Component
public class WikipediaRestClient implements WikipediaClient {

  private final RestClientFactory restClientFactory;
  private final String baseUrlTemplate;

  public WikipediaRestClient(
    RestClientFactory restClientFactory,
    ExternalApisProperties properties
  ) {
    this.restClientFactory = restClientFactory;
    this.baseUrlTemplate = properties.wikipediaBaseUrlTemplate();
  }

  @Override
  @Retry(name = "wikipediaApi")
  @CircuitBreaker(name = "wikipediaApi")
  @Bulkhead(name = "wikipediaApi")
  public WikipediaSummaryDTO getSummary(String languageCode, String title) {
    String baseUrl = String.format(Locale.ROOT, baseUrlTemplate, languageCode);
    RestClient restClient = restClientFactory.builderFor(baseUrl).build();
    try {
      return restClient
        .get()
        .uri("/page/summary/{title}", title)
        .retrieve()
        .body(WikipediaSummaryDTO.class);
    } catch (HttpClientErrorException.NotFound e) {
      return null;
    }
  }
}
