package br.usp.lab.oo.planejador_feriado.recommendation.filter;

import java.util.Locale;
import java.util.Optional;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(20)
public class MaxGroundBudgetFilter extends CandidateFilter {

  @Override
  protected Optional<String> check(FilterContext context) {
    Double maximum = context.request().maxGroundBudgetBrl();
    if (maximum == null || context.groundCost() == null) {
      return Optional.empty();
    }
    if (context.groundCost().estimatedTotal() <= maximum) {
      return Optional.empty();
    }
    return Optional.of(
      String.format(
        Locale.ROOT,
        "Estimativa terrestre de R$ %.2f acima do orçamento de R$ %.2f",
        context.groundCost().estimatedTotal(),
        maximum
      )
    );
  }
}
