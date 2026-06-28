package br.usp.lab.oo.planejador_feriado.cost.service;

import br.usp.lab.oo.planejador_feriado.common.worldbank.WorldBankIndicatorClient;
import br.usp.lab.oo.planejador_feriado.common.worldbank.WorldBankIndicatorPoint;
import br.usp.lab.oo.planejador_feriado.cost.model.CostOfLiving;
import java.time.Year;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Calcula o nível de preços (proxy de custo de vida) de um país relativo aos EUA,
 * como {@code fator de conversão PPP (consumo) ÷ câmbio oficial de mercado}, ambos do
 * Banco Mundial, no ano mais recente em que os dois indicadores existem. Valores abaixo
 * de 1 indicam preços mais baratos que os americanos. (O antigo indicador único de nível
 * de preços, PA.NUS.PPPC.RF, foi arquivado pelo Banco Mundial.)
 */
@Service
public class CostOfLivingService {

  private static final String PPP_INDICATOR = "PA.NUS.PRVT.PP"; // PPP conversion factor, private consumption
  private static final String FX_INDICATOR = "PA.NUS.FCRF"; // Official exchange rate (LCU per US$)

  /**
   * Países que passaram por redenominação de moeda (ex.: Venezuela, com o bolívar
   * trocado em 2008 e 2018) podem ter a série de PPP "congelada" num ano anterior à
   * troca, enquanto o câmbio oficial segue atualizado na moeda nova — dividir um pelo
   * outro produz uma razão sem sentido (ordens de grandeza fora da realidade, ex.
   * ~1e-11 em vez de algo entre 0.01 e 10). Em vez de tentar detectar esse caso
   * especificamente, qualquer ano mais antigo que isso é tratado como dado ausente:
   * na prática só descarta exatamente esses artefatos de pré-redenominação, já que o
   * Banco Mundial normalmente atualiza os dois indicadores com poucos anos de atraso.
   */
  private static final int MAX_DATA_AGE_YEARS = 8;

  private final WorldBankIndicatorClient client;

  public CostOfLivingService(WorldBankIndicatorClient client) {
    this.client = client;
  }

  public Optional<CostOfLiving> getPriceLevel(String isoCode) {
    Map<String, Double> ppp = toYearMap(
      client.getIndicatorSeries(isoCode, PPP_INDICATOR)
    );
    Map<String, Double> exchange = toYearMap(
      client.getIndicatorSeries(isoCode, FX_INDICATOR)
    );
    int currentYear = Year.now().getValue();

    return ppp
      .keySet()
      .stream()
      .filter(exchange::containsKey)
      .filter(year -> exchange.get(year) != 0.0)
      .filter(year -> isRecentEnough(year, currentYear))
      .max(Comparator.naturalOrder())
      .map(year ->
        new CostOfLiving(isoCode, ppp.get(year) / exchange.get(year), year)
      );
  }

  private static boolean isRecentEnough(String year, int currentYear) {
    try {
      return currentYear - Integer.parseInt(year) <= MAX_DATA_AGE_YEARS;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  private Map<String, Double> toYearMap(List<WorldBankIndicatorPoint> series) {
    if (series == null) {
      return Map.of();
    }
    return series
      .stream()
      .filter(point -> point.year() != null && point.value() != null)
      .collect(
        Collectors.toMap(
          WorldBankIndicatorPoint::year,
          WorldBankIndicatorPoint::value,
          (a, b) -> a
        )
      );
  }
}
