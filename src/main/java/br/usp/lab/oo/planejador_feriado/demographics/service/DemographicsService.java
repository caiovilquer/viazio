package br.usp.lab.oo.planejador_feriado.demographics.service;

import br.usp.lab.oo.planejador_feriado.common.worldbank.WorldBankIndicatorClient;
import br.usp.lab.oo.planejador_feriado.common.worldbank.WorldBankIndicatorPoint;
import br.usp.lab.oo.planejador_feriado.demographics.model.Demographics;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Consulta a população total de um país (indicador {@code SP.POP.TOTL} do Banco
 * Mundial), reaproveitando o mesmo {@link WorldBankIndicatorClient} usado pelo custo
 * de vida — é apenas mais uma série anual da mesma fonte.
 */
@Service
public class DemographicsService {

    private static final String POPULATION_INDICATOR = "SP.POP.TOTL";

    private final WorldBankIndicatorClient client;

    public DemographicsService(WorldBankIndicatorClient client) {
        this.client = client;
    }

    public Optional<Demographics> getPopulation(String isoCode) {
        List<WorldBankIndicatorPoint> series = client.getIndicatorSeries(isoCode, POPULATION_INDICATOR);
        return series.stream()
                .filter(point -> point.year() != null && point.value() != null)
                .max(Comparator.comparing(WorldBankIndicatorPoint::year))
                .map(point -> new Demographics(isoCode, Math.round(point.value()), point.year()));
    }
}
