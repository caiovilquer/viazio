package br.usp.lab.oo.planejador_feriado.common.worldbank;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Decorator (GoF) que adiciona cache em memória sobre {@link WorldBankClient}.
 * Os indicadores são anuais, então o TTL é longo — evita repetir a consulta para o
 * mesmo país/indicador ao comparar vários destinos.
 */
@Primary
@Component
public class CachingWorldBankIndicatorClient
  implements WorldBankIndicatorClient
{

  private final WorldBankIndicatorClient delegate;
  private final Cache<String, List<WorldBankIndicatorPoint>> cache;

  public CachingWorldBankIndicatorClient(WorldBankClient delegate) {
    this.delegate = delegate;
    this.cache = Caffeine.newBuilder()
      .maximumSize(1000)
      .expireAfterWrite(Duration.ofDays(7))
      .build();
  }

  @Override
  public List<WorldBankIndicatorPoint> getIndicatorSeries(
    String isoCode,
    String indicatorCode
  ) {
    String key = isoCode.toUpperCase(Locale.ROOT) + "|" + indicatorCode;
    return cache.get(key, k ->
      delegate.getIndicatorSeries(isoCode, indicatorCode)
    );
  }
}
