package br.usp.lab.oo.planejador_feriado.exchange.client;

import br.usp.lab.oo.planejador_feriado.exchange.dto.ExchangeDTO;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;

/**
 * Decorator (GoF) que adiciona cache em memória sobre {@link AwesomeApiClient}.
 * Câmbio varia rápido, então o TTL é curto (apenas para evitar chamadas
 * duplicadas dentro de uma mesma comparação de destinos), bem menor que o
 * cache de país/feriados.
 */
@Primary
@Component
public class CachingExchangeClient implements ExchangeClient {

    private final ExchangeClient delegate;
    private final Cache<String, Map<String, ExchangeDTO>> cache;

    public CachingExchangeClient(AwesomeApiClient delegate) {
        this.delegate = delegate;
        this.cache = Caffeine.newBuilder()
                .maximumSize(200)
                .expireAfterWrite(Duration.ofMinutes(5))
                .build();
    }

    @Override
    public Map<String, ExchangeDTO> getExchangeRate(String currencyCode) {
        String key = currencyCode.toUpperCase(Locale.ROOT);
        return cache.get(key, k -> delegate.getExchangeRate(currencyCode));
    }
}
