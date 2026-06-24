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
        @NotNull(message = "é obrigatório")
        @Schema(example = "2026-09-04")
        LocalDate from,

        @NotNull(message = "é obrigatório")
        @Schema(example = "2026-09-07")
        LocalDate to,

        @Size(
                max = RecommendationLimits.MAX_EXPLICIT_CANDIDATES,
                message = "aceita no máximo 50 países")
        List<
                @NotBlank(message = "não pode ser vazio")
                @Pattern(regexp = "(?i)[A-Z]{2}", message = "deve usar código ISO alpha-2") String> countries,

        @Size(max = 50, message = "deve ter no máximo 50 caracteres")
        @Schema(example = "Americas")
        String region,

        @Min(value = 1, message = "deve ser no mínimo 1")
        @Max(value = RecommendationLimits.MAX_RESULTS, message = "deve ser no máximo 15")
        @Schema(defaultValue = "10")
        Integer limit,

        @Size(max = 50, message = "deve ter no máximo 50 caracteres")
        @Schema(example = "economico")
        String profile,

        @Size(max = 4, message = "aceita no máximo 4 critérios")
        Map<
                @NotBlank(message = "não pode ser vazio")
                @Pattern(regexp = "(?i)(weather|cost|distance|festivities)",
                        message = "critério desconhecido") String,
                @NotNull(message = "é obrigatório")
                @DecimalMin(value = "0.0", message = "deve ser maior ou igual a 0")
                @DecimalMax(value = "1.0", message = "deve ser menor ou igual a 1") Double> weights,

        @Size(
                max = RecommendationLimits.MAX_EXPLICIT_CANDIDATES,
                message = "aceita no máximo 50 exclusões")
        List<
                @NotBlank(message = "não pode ser vazio")
                @Pattern(regexp = "(?i)[A-Z]{2}", message = "deve usar código ISO alpha-2") String> exclude,

        @Valid
        OriginInput origin,

        @Min(value = 1, message = "deve ser no mínimo 1")
        @Max(value = RecommendationLimits.MAX_TRAVELERS, message = "deve ser no máximo 10")
        @Schema(defaultValue = "1")
        Integer travelers,

        @Positive(message = "deve ser um valor positivo")
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
