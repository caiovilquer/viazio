package br.usp.lab.oo.planejador_feriado.recommendation.filter;

import br.usp.lab.oo.planejador_feriado.exchange.model.Exchange;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;

/**
 * Descarta candidatos cuja moeda esteja acima do teto de câmbio informado
 * ({@code maxExchangeRate}) — o filtro de orçamento do usuário.
 */
@Component
@Order(20)
public class MaxExchangeRateFilter extends CandidateFilter {

    @Override
    protected Optional<String> check(FilterContext context) {
        Double maxRate = context.request().maxExchangeRate();
        Exchange exchange = context.exchangeToBrl();

        if (maxRate == null || exchange == null) {
            return Optional.empty();
        }

        double rate = exchange.getValueInReais();
        if (rate > maxRate) {
            return Optional.of(String.format(Locale.ROOT,
                    "Acima do orçamento: 1 %s = R$ %.2f (máx. R$ %.2f)",
                    exchange.getCurrency(), rate, maxRate));
        }
        return Optional.empty();
    }
}
