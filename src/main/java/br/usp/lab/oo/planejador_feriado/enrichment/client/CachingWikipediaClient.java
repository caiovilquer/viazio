package br.usp.lab.oo.planejador_feriado.enrichment.client;

import br.usp.lab.oo.planejador_feriado.enrichment.dto.WikipediaSummaryDTO;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Decorator (GoF) que adiciona cache em memória sobre {@link WikipediaRestClient}.
 * Resumos de país praticamente não mudam, então o TTL é longo; o cache guarda também
 * o resultado "não encontrado" (negative caching) para não repetir a busca a cada
 * recomendação para destinos sem artigo correspondente.
 */
@Primary
@Component
public class CachingWikipediaClient implements WikipediaClient {

  private final WikipediaClient delegate;
  private final Cache<String, Optional<WikipediaSummaryDTO>> cache;

  public CachingWikipediaClient(WikipediaRestClient delegate) {
    this.delegate = delegate;
    this.cache = Caffeine.newBuilder()
      .maximumSize(1000)
      .expireAfterWrite(Duration.ofDays(30))
      .build();
  }

  @Override
  public WikipediaSummaryDTO getSummary(String languageCode, String title) {
    String key = languageCode.toLowerCase(Locale.ROOT) + "|" + title;
    return cache
      .get(key, k ->
        Optional.ofNullable(delegate.getSummary(languageCode, title))
      )
      .orElse(null);
  }
}
