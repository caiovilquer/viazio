package br.usp.lab.oo.planejador_feriado.country.client;

import br.usp.lab.oo.planejador_feriado.country.dto.CountryDTO;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Decorator (GoF) que adiciona cache em memória sobre {@link StaticCountryClient}.
 * O dataset de países é local e estável, então o cache apenas evita refiltrar a
 * lista a cada consulta (ex.: por região) durante uma comparação de destinos.
 */
@Primary
@Component
public class CachingCountryClient implements CountryClient {

  private final CountryClient delegate;
  private final Cache<String, List<CountryDTO>> cache;

  public CachingCountryClient(StaticCountryClient delegate) {
    this.delegate = delegate;
    this.cache = Caffeine.newBuilder()
      .maximumSize(500)
      .expireAfterWrite(Duration.ofHours(24))
      .build();
  }

  @Override
  public List<CountryDTO> getAllCountries() {
    return cache.get("all", key -> delegate.getAllCountries());
  }

  @Override
  public List<CountryDTO> getCountryByCode(String countryCode) {
    return cache.get("code:" + countryCode.toUpperCase(Locale.ROOT), key ->
      delegate.getCountryByCode(countryCode)
    );
  }

  @Override
  public List<CountryDTO> getCountryByName(String countryName) {
    return cache.get("name:" + countryName.toLowerCase(Locale.ROOT), key ->
      delegate.getCountryByName(countryName)
    );
  }

  @Override
  public List<CountryDTO> getCountriesByRegion(String region) {
    return cache.get("region:" + region.toLowerCase(Locale.ROOT), key ->
      delegate.getCountriesByRegion(region)
    );
  }
}
