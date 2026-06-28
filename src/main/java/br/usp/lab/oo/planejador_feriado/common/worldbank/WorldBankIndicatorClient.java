package br.usp.lab.oo.planejador_feriado.common.worldbank;

import java.util.List;

/**
 * Abstrai o acesso a séries de indicadores do Banco Mundial (usado tanto para custo de
 * vida/PPP quanto para população), permitindo decorar a implementação real (ex.: com
 * cache) sem que os serviços de domínio conheçam o detalhe.
 */
public interface WorldBankIndicatorClient {
  /** Série anual (mais novo primeiro) de um indicador para o país ISO informado. */
  List<WorldBankIndicatorPoint> getIndicatorSeries(
    String isoCode,
    String indicatorCode
  );
}
