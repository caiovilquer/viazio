package br.usp.lab.oo.planejador_feriado.recommendation.filter;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Descarta candidatos que o usuário pediu para excluir explicitamente da comparação.
 * Roda primeiro na cadeia por ser o filtro mais barato (não depende de câmbio).
 */
@Component
@Order(10)
public class ExcludedCountriesFilter extends CandidateFilter {

  @Override
  protected Optional<String> check(FilterContext context) {
    Set<String> excluded = context
      .request()
      .excludedCountryCodes()
      .stream()
      .map(code -> code.toUpperCase(Locale.ROOT))
      .collect(Collectors.toSet());

    if (excluded.contains(context.countryCode().toUpperCase(Locale.ROOT))) {
      return Optional.of("Excluído pelo usuário");
    }
    return Optional.empty();
  }
}
