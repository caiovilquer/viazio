package br.usp.lab.oo.planejador_feriado.cost.service;

import br.usp.lab.oo.planejador_feriado.cost.client.CostOfLivingClient;
import br.usp.lab.oo.planejador_feriado.cost.dto.WorldBankIndicatorPoint;
import br.usp.lab.oo.planejador_feriado.cost.model.CostOfLiving;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Calcula o nível de preços (proxy de custo de vida) de um país relativo aos EUA,
 * como {@code fator de conversão PPP (consumo) ÷ câmbio oficial de mercado}, ambos do
 * Banco Mundial, no ano mais recente em que os dois indicadores existem. Valores abaixo
 * de 1 indicam preços mais baratos que os americanos. (O antigo indicador único de nível
 * de preços, PA.NUS.PPPC.RF, foi arquivado pelo Banco Mundial.)
 */
@Service
public class CostOfLivingService {

    private static final String PPP_INDICATOR = "PA.NUS.PRVT.PP";   // PPP conversion factor, private consumption
    private static final String FX_INDICATOR = "PA.NUS.FCRF";       // Official exchange rate (LCU per US$)

    private final CostOfLivingClient client;

    public CostOfLivingService(CostOfLivingClient client) {
        this.client = client;
    }

    public Optional<CostOfLiving> getPriceLevel(String isoCode) {
        Map<String, Double> ppp = toYearMap(client.getIndicatorSeries(isoCode, PPP_INDICATOR));
        Map<String, Double> exchange = toYearMap(client.getIndicatorSeries(isoCode, FX_INDICATOR));

        return ppp.keySet().stream()
                .filter(exchange::containsKey)
                .filter(year -> exchange.get(year) != 0.0)
                .max(Comparator.naturalOrder())
                .map(year -> new CostOfLiving(isoCode, ppp.get(year) / exchange.get(year), year));
    }

    private Map<String, Double> toYearMap(List<WorldBankIndicatorPoint> series) {
        if (series == null) {
            return Map.of();
        }
        return series.stream()
                .filter(point -> point.year() != null && point.value() != null)
                .collect(Collectors.toMap(
                        WorldBankIndicatorPoint::year,
                        WorldBankIndicatorPoint::value,
                        (a, b) -> a));
    }
}
