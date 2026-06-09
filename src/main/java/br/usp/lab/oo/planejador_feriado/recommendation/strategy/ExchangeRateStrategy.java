package br.usp.lab.oo.planejador_feriado.recommendation.strategy;

import br.usp.lab.oo.planejador_feriado.exchange.model.Exchange;
import br.usp.lab.oo.planejador_feriado.recommendation.model.RecommendationContext;
import br.usp.lab.oo.planejador_feriado.recommendation.model.ScoreEntry;
import org.springframework.stereotype.Component;

@Component
public class ExchangeRateStrategy implements ScoringStrategy {

    private static final double MAX_POINTS = 35.0;
    private static final String CRITERION = "CAMBIO";

    @Override
    public ScoreEntry evaluate(RecommendationContext context) {
        Exchange exchange = context.exchangeToBrl();
        Double maxRate = context.request().maxExchangeRate();

        if (exchange == null) {
            return new ScoreEntry(
                    CRITERION,
                    0.0,
                    MAX_POINTS,
                    "Câmbio indisponível ou moeda local é BRL"
            );
        }

        double rate = exchange.getValueInReais();
        if (maxRate != null && rate > maxRate) {
            return new ScoreEntry(
                    CRITERION,
                    0.0,
                    MAX_POINTS,
                    String.format("Acima do orçamento: 1 %s = R$ %.2f (máx. R$ %.2f)",
                            exchange.getCurrency(), rate, maxRate)
            );
        }

        double points = pointsForRate(rate);
        String justification = justificationForRate(exchange.getCurrency(), rate, points);

        return new ScoreEntry(CRITERION, points, MAX_POINTS, justification);
    }

    private double pointsForRate(double rate) {
        if (rate <= 1.00) {
            return 35.0;
        }
        if (rate <= 3.00) {
            return 25.0;
        }
        if (rate <= 5.00) {
            return 15.0;
        }
        return 8.0;
    }

    private String justificationForRate(String currency, double rate, double points) {
        if (points >= 35.0) {
            return String.format("Câmbio muito favorável: 1 %s = R$ %.2f", currency, rate);
        }
        if (points >= 25.0) {
            return String.format("Câmbio favorável: 1 %s = R$ %.2f", currency, rate);
        }
        if (points >= 15.0) {
            return String.format("Câmbio moderado: 1 %s = R$ %.2f", currency, rate);
        }
        return String.format("Câmbio desfavorável: 1 %s = R$ %.2f", currency, rate);
    }
}
