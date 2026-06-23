package br.usp.lab.oo.planejador_feriado.holiday.client;

import br.usp.lab.oo.planejador_feriado.holiday.dto.HolidayDTO;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

/**
 * Decorator (GoF) que adiciona cache em memória sobre {@link NagerDateClient}.
 * O calendário de feriados de um ano não muda durante o dia, e o motor de
 * recomendação busca o calendário do Brasil repetidamente ao comparar destinos —
 * cachear evita N chamadas idênticas em uma única requisição de comparação.
 */
@Primary
@Component
public class CachingHolidayClient implements HolidayClient {

    private final HolidayClient delegate;
    private final Cache<String, List<HolidayDTO>> cache;

    public CachingHolidayClient(NagerDateClient delegate) {
        this.delegate = delegate;
        this.cache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(Duration.ofHours(12))
                .build();
    }

    @Override
    public List<HolidayDTO> getPublicHolidays(int year, String countryCode) {
        String key = year + ":" + countryCode.toUpperCase(Locale.ROOT);
        return cache.get(key, k -> delegate.getPublicHolidays(year, countryCode));
    }
}
