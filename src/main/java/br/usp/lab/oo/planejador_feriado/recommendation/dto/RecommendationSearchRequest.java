package br.usp.lab.oo.planejador_feriado.recommendation.dto;

import br.usp.lab.oo.planejador_feriado.recommendation.config.RecommendationLimits;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Schema(description = "Busca estruturada de recomendações para consumo por frontend")
public record RecommendationSearchRequest(
        @NotNull
        @Schema(example = "2026-09-04")
        LocalDate from,

        @NotNull
        @Schema(example = "2026-09-07")
        LocalDate to,

        @Size(max = RecommendationLimits.MAX_EXPLICIT_CANDIDATES)
        List<
                @NotBlank
                @Pattern(regexp = "(?i)[A-Z]{2}", message = "deve usar código ISO alpha-2") String> countries,

        @Size(max = 50)
        @Schema(example = "Americas")
        String region,

        @Min(1)
        @Max(RecommendationLimits.MAX_RESULTS)
        @Schema(defaultValue = "10")
        Integer limit,

        @Size(max = RecommendationLimits.MAX_EXPLICIT_CANDIDATES)
        @Schema(example = "economico")
        String profile,

        @Size(max = 4)
        Map<
                @NotBlank
                @Pattern(regexp = "(?i)(weather|cost|distance|festivities)",
                        message = "critério desconhecido") String,
                @NotNull @DecimalMin("0.0") @DecimalMax("1.0") Double> weights,

        @Size(max = 50)
        List<
                @NotBlank
                @Pattern(regexp = "(?i)[A-Z]{2}", message = "deve usar código ISO alpha-2") String> exclude,

        @Valid
        OriginInput origin,

        @Min(1)
        @Max(RecommendationLimits.MAX_TRAVELERS)
        @Schema(defaultValue = "1")
        Integer travelers,

        @Positive
        @Schema(example = "5000")
        Double maxGroundBudgetBrl
) {
    public RecommendationSearchRequest {
        countries = countries != null
                ? Collections.unmodifiableList(new ArrayList<>(countries))
                : List.of();
        weights = weights != null
                ? Collections.unmodifiableMap(new LinkedHashMap<>(weights))
                : Map.of();
        exclude = exclude != null
                ? Collections.unmodifiableList(new ArrayList<>(exclude))
                : List.of();
    }

    public int limitOrDefault() {
        return limit != null ? limit : 10;
    }

    public int travelersOrDefault() {
        return travelers != null ? travelers : 1;
    }

    public OriginInput originOrDefault() {
        return origin != null ? origin : new OriginInput("BR", null, null, null, null);
    }
}
