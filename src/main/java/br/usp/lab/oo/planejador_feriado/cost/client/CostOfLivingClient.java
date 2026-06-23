package br.usp.lab.oo.planejador_feriado.cost.client;

import br.usp.lab.oo.planejador_feriado.cost.dto.WorldBankIndicatorPoint;

import java.util.List;

/**
 * Abstrai o acesso a indicadores de custo de vida (Banco Mundial), permitindo decorar
 * a implementação real (ex.: com cache) sem que {@code CostOfLivingService} conheça o detalhe.
 */
public interface CostOfLivingClient {

    /** Pontos recentes (mais novo primeiro) do nível de preços PPP para o país ISO informado. */
    List<WorldBankIndicatorPoint> getPriceLevelSeries(String isoCode);
}
