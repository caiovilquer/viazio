package br.usp.lab.oo.planejador_feriado.cost.client;

import br.usp.lab.oo.planejador_feriado.cost.dto.WorldBankIndicatorPoint;

import java.util.List;

/**
 * Abstrai o acesso a séries de indicadores do Banco Mundial, permitindo decorar a
 * implementação real (ex.: com cache) sem que {@code CostOfLivingService} conheça o detalhe.
 */
public interface CostOfLivingClient {

    /** Série anual (mais novo primeiro) de um indicador para o país ISO informado. */
    List<WorldBankIndicatorPoint> getIndicatorSeries(String isoCode, String indicatorCode);
}
