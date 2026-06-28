package br.usp.lab.oo.planejador_feriado.recommendation.service;

import br.usp.lab.oo.planejador_feriado.holiday.HolidayDeduplicator;
import br.usp.lab.oo.planejador_feriado.holiday.model.Holiday;
import br.usp.lab.oo.planejador_feriado.holiday.service.HolidayService;
import br.usp.lab.oo.planejador_feriado.recommendation.detector.LongWeekendDetector;
import br.usp.lab.oo.planejador_feriado.recommendation.dto.BestWindowsResponse;
import br.usp.lab.oo.planejador_feriado.recommendation.dto.WindowSuggestion;
import br.usp.lab.oo.planejador_feriado.recommendation.model.BestWindowsRequest;
import br.usp.lab.oo.planejador_feriado.recommendation.model.LongWeekend;
import br.usp.lab.oo.planejador_feriado.recommendation.model.RecommendationRequest;
import br.usp.lab.oo.planejador_feriado.recommendation.model.TravelRecommendation;
import br.usp.lab.oo.planejador_feriado.recommendation.model.WindowAssessment;
import br.usp.lab.oo.planejador_feriado.recommendation.weight.WeightResolver;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Descobre proativamente as melhores janelas de viagem (feriadões/pontes do calendário
 * da origem) num período amplo, em vez de exigir que o usuário adivinhe as datas.
 * Reaproveita o {@link LongWeekendDetector} para achar os feriadões e o
 * {@link TravelRecommendationEngine} para rankear destinos dentro de cada janela.
 */
@Service
public class BestWindowsService {

  private final HolidayService holidayService;
  private final LongWeekendDetector longWeekendDetector;
  private final TravelWindowEvaluator windowEvaluator;
  private final TravelRecommendationEngine recommendationEngine;
  private final WeightResolver weightResolver;

  public BestWindowsService(
    HolidayService holidayService,
    LongWeekendDetector longWeekendDetector,
    TravelWindowEvaluator windowEvaluator,
    TravelRecommendationEngine recommendationEngine,
    WeightResolver weightResolver
  ) {
    this.holidayService = holidayService;
    this.longWeekendDetector = longWeekendDetector;
    this.windowEvaluator = windowEvaluator;
    this.recommendationEngine = recommendationEngine;
    this.weightResolver = weightResolver;
  }

  public BestWindowsResponse findBestWindows(BestWindowsRequest request) {
    List<Holiday> originHolidays = HolidayDeduplicator.deduplicate(
      holidayService.getHolidaysInWindow(
        request.originCountryCode(),
        request.originSubdivisionCode(),
        request.from(),
        request.to()
      )
    );
    List<LongWeekend> longWeekends = longWeekendDetector.detect(
      originHolidays,
      request.from(),
      request.to()
    );

    List<WindowSuggestion> windows = longWeekends
      .stream()
      .filter(window -> window.totalDays() >= request.minDays())
      .sorted(
        Comparator.comparingDouble((LongWeekend window) ->
          windowEvaluator
            .evaluate(originHolidays, window.start(), window.end())
            .score()
        ).reversed()
      )
      .limit(Math.max(request.topWindows(), 0))
      .map(window -> toSuggestion(window, request, originHolidays))
      .toList();

    String profile = weightResolver
      .resolve(request.profile(), request.weightOverrides())
      .profileName();
    return new BestWindowsResponse(
      request.from(),
      request.to(),
      profile,
      windows
    );
  }

  private WindowSuggestion toSuggestion(
    LongWeekend window,
    BestWindowsRequest request,
    List<Holiday> originHolidays
  ) {
    List<TravelRecommendation> topDestinations = List.of();
    if (request.hasCandidates()) {
      RecommendationRequest windowRequest = new RecommendationRequest(
        window.start(),
        window.end(),
        request.countryCodes(),
        request.region(),
        request.destinationsPerWindow(),
        request.profile(),
        request.weightOverrides(),
        request.excludedCountryCodes(),
        request.originCountryCode(),
        request.originSubdivisionCode(),
        request.originLatitude(),
        request.originLongitude(),
        request.originCityName(),
        request.travelers(),
        request.maxGroundBudgetBrl()
      );
      topDestinations = recommendationEngine
        .recommend(windowRequest)
        .recommendations();
    }

    WindowAssessment assessment = windowEvaluator.evaluate(
      originHolidays,
      window.start(),
      window.end()
    );
    return new WindowSuggestion(
      window.start(),
      window.end(),
      window.totalDays(),
      window.bridgeDaysUsed(),
      assessment.requiredLeaveDays(),
      label(window),
      assessment.score(),
      topDestinations
    );
  }

  private String label(LongWeekend window) {
    StringBuilder builder = new StringBuilder();
    builder
      .append("Feriadão de ")
      .append(window.totalDays())
      .append(" dias (")
      .append(window.holidayName());
    if (window.bridgeDaysUsed() > 0) {
      builder.append(" + ponte");
    }
    builder.append(")");
    return builder.toString();
  }
}
