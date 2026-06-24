package br.usp.lab.oo.planejador_feriado.recommendation.strategy;

import br.usp.lab.oo.planejador_feriado.exchange.model.Exchange;
import br.usp.lab.oo.planejador_feriado.recommendation.model.Criterion;
import br.usp.lab.oo.planejador_feriado.recommendation.model.RecommendationContext;
import br.usp.lab.oo.planejador_feriado.recommendation.model.ScoreEntry;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Avalia o câmbio nominal da moeda do destino para o real. É o sentimento de
 * "quanto custa comprar a moeda de lá" — complementado pelo critério de custo de
 * vida (PPP), que corrige o engano de que moeda barata por unidade significa país
 * barato. O teto de orçamento ({@code maxExchangeRate}) é tratado antes, como filtro.
 */
@Component
public class ExchangeRateStrategy implements ScoringStrategy {

    @Override
    public Criterion criterion() {
        return Criterion.EXCHANGE;
    }

    @Override
    public ScoreEntry evaluate(RecommendationContext context) {
        Exchange exchange = context.exchangeToBrl();
        if (exchange == null) {
            return ScoreEntry.unavailable(criterion(), "Câmbio indisponível ou moeda local é BRL");
        }

        double rate = exchange.getValueInReais();
        double score = scoreForRate(rate);
        String formattedRate = rate < 1.0
                ? String.format(Locale.ROOT, "%.4f", rate)
                : String.format(Locale.ROOT, "%.2f", rate);
        String justification = String.format(Locale.ROOT,
                "%s: 1 %s = R$ %s", qualitative(score), exchange.getCurrency(), formattedRate);
        return ScoreEntry.of(criterion(), score, justification);
    }

    private double scoreForRate(double rate) {
        if (rate <= 1.0) {
            return 100.0;
        }
        if (rate <= 3.0) {
            return 70.0;
        }
        if (rate <= 5.0) {
            return 45.0;
        }
        if (rate <= 8.0) {
            return 25.0;
        }
        return 12.0;
    }

    private String qualitative(double score) {
        if (score >= 90.0) {
            return "Câmbio muito favorável";
        }
        if (score >= 60.0) {
            return "Câmbio favorável";
        }
        if (score >= 40.0) {
            return "Câmbio moderado";
        }
        return "Câmbio desfavorável";
    }
}
