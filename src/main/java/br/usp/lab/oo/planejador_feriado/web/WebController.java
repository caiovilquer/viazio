package br.usp.lab.oo.planejador_feriado.web;

import br.usp.lab.oo.planejador_feriado.country.service.CountryService;
import br.usp.lab.oo.planejador_feriado.recommendation.dto.RecommendationResponse;
import br.usp.lab.oo.planejador_feriado.recommendation.model.RecommendationRequest;
import br.usp.lab.oo.planejador_feriado.recommendation.service.TravelRecommendationEngine;
import br.usp.lab.oo.planejador_feriado.travel.model.TravelOverview;
import br.usp.lab.oo.planejador_feriado.travel.service.TravelService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Controller
public class WebController {

    private static final int MAX_LIMIT = 15;
    private static final int MAX_WINDOW_DAYS = 92;

    private final TravelService travelService;
    private final TravelRecommendationEngine recommendationEngine;

    public WebController(TravelService travelService, TravelRecommendationEngine recommendationEngine) {
        this.travelService = travelService;
        this.recommendationEngine = recommendationEngine;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/viagem")
    public String viagem(
            @RequestParam(name = "destino", defaultValue = "") String destino,
            @RequestParam(name = "codigo", required = false) String codigo,
            Model model) {

        String query = resolveDestinationQuery(destino, codigo);
        if (query.isBlank()) {
            model.addAttribute("erro", "Informe o destino: código ISO de duas letras (ex.: BR, JP) ou nome em inglês (ex.: japan, france).");
            return "resultado";
        }

        boolean isCodeQuery = CountryService.looksLikeIsoCode(query);

        try {
            TravelOverview overview = travelService.getOverviewByQuery(query);
            model.addAttribute("overview", overview);
            model.addAttribute("termoBuscado", overview.country().getName());
        } catch (RuntimeException e) {
            if (isCodeQuery) {
                model.addAttribute("erro",
                        "País não encontrado para o código \"" + query.toUpperCase(Locale.ROOT)
                                + "\". Verifique se o código ISO está correto (ex.: BR, JP, US, FR).");
            } else {
                model.addAttribute("erro",
                        "País não encontrado para \"" + query
                                + "\". Tente o nome em inglês (ex.: japan, france) ou o código ISO (ex.: JP, FR).");
            }
            model.addAttribute("termoBuscado", isCodeQuery ? query.toUpperCase(Locale.ROOT) : query);
        }

        return "resultado";
    }

    private String resolveDestinationQuery(String destino, String codigo) {
        if (destino != null && !destino.isBlank()) {
            return destino.trim();
        }
        if (codigo != null && !codigo.isBlank()) {
            return codigo.trim();
        }
        return "";
    }

    @GetMapping("/recomendacoes")
    public String recomendacoes(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String countries,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) Double maxRate,
            @RequestParam(required = false, defaultValue = "10") int limit,
            Model model) {

        if (from == null || from.isBlank() || to == null || to.isBlank()) {
            return "redirect:/";
        }

        LocalDate fromDate;
        LocalDate toDate;
        try {
            fromDate = LocalDate.parse(from.trim());
            toDate = LocalDate.parse(to.trim());
        } catch (DateTimeParseException e) {
            model.addAttribute("erro", "Informe datas válidas no formato AAAA-MM-DD.");
            return "recomendacoes-resultado";
        }

        String validationError = validateRecommendationInput(fromDate, toDate, countries, region, limit);
        if (validationError != null) {
            model.addAttribute("erro", validationError);
            model.addAttribute("fromFormatado", fromDate);
            model.addAttribute("toFormatado", toDate);
            return "recomendacoes-resultado";
        }

        List<String> countryCodes = parseCountries(countries);
        RecommendationRequest request = new RecommendationRequest(
                fromDate,
                toDate,
                countryCodes,
                normalizeRegion(region),
                maxRate,
                limit
        );

        try {
            RecommendationResponse resultado = recommendationEngine.recommend(request);
            model.addAttribute("resultado", resultado);
            model.addAttribute("fromFormatado", fromDate);
            model.addAttribute("toFormatado", toDate);
        } catch (RuntimeException e) {
            model.addAttribute("erro", "Não foi possível gerar as recomendações no momento. Tente novamente em instantes.");
            model.addAttribute("fromFormatado", fromDate);
            model.addAttribute("toFormatado", toDate);
        }

        return "recomendacoes-resultado";
    }

    private String validateRecommendationInput(
            LocalDate from,
            LocalDate to,
            String countries,
            String region,
            int limit) {

        if (from.isAfter(to)) {
            return "A data inicial deve ser anterior ou igual à data final.";
        }

        long days = ChronoUnit.DAYS.between(from, to) + 1;
        if (days > MAX_WINDOW_DAYS) {
            return "A janela máxima permitida é de " + MAX_WINDOW_DAYS + " dias.";
        }

        boolean hasCountries = countries != null && !countries.isBlank();
        boolean hasRegion = region != null && !region.isBlank();
        if (hasCountries == hasRegion) {
            return "Informe exatamente um modo: lista de países (códigos ISO) ou região.";
        }

        if (limit < 1 || limit > MAX_LIMIT) {
            return "O limite de resultados deve estar entre 1 e " + MAX_LIMIT + ".";
        }

        return null;
    }

    private List<String> parseCountries(String countries) {
        if (countries == null || countries.isBlank()) {
            return List.of();
        }

        return Arrays.stream(countries.split(","))
                .map(String::trim)
                .filter(code -> !code.isBlank())
                .map(code -> code.toUpperCase(Locale.ROOT))
                .collect(Collectors.toList());
    }

    private String normalizeRegion(String region) {
        if (region == null || region.isBlank()) {
            return null;
        }
        return region.trim();
    }
}
