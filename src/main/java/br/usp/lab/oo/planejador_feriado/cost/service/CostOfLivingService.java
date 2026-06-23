package br.usp.lab.oo.planejador_feriado.cost.service;

import br.usp.lab.oo.planejador_feriado.cost.client.CostOfLivingClient;
import br.usp.lab.oo.planejador_feriado.cost.dto.WorldBankIndicatorPoint;
import br.usp.lab.oo.planejador_feriado.cost.model.CostOfLiving;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Expõe o nível de preços (proxy de custo de vida) de um país a partir do indicador
 * PPP do Banco Mundial, escolhendo o ponto mais recente com valor disponível.
 */
@Service
public class CostOfLivingService {

    private final CostOfLivingClient client;

    public CostOfLivingService(CostOfLivingClient client) {
        this.client = client;
    }

    public Optional<CostOfLiving> getPriceLevel(String isoCode) {
        List<WorldBankIndicatorPoint> series = client.getPriceLevelSeries(isoCode);
        if (series == null) {
            return Optional.empty();
        }

        return series.stream()
                .filter(point -> point.value() != null)
                .findFirst()
                .map(point -> new CostOfLiving(isoCode, point.value(), point.year()));
    }
}
