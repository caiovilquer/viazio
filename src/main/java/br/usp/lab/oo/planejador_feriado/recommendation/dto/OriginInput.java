package br.usp.lab.oo.planejador_feriado.recommendation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Referência geográfica e de calendário da origem da viagem")
public record OriginInput(
        @Pattern(regexp = "(?i)[A-Z]{2}", message = "deve usar código ISO alpha-2")
        @Schema(example = "BR")
        String countryCode,

        @Pattern(
                regexp = "(?i)[A-Z]{2}-[A-Z0-9]{1,3}",
                message = "deve usar ISO 3166-2, por exemplo BR-SP")
        @Schema(example = "BR-SP")
        String subdivisionCode,

        @DecimalMin(value = "-90.0", message = "deve ser maior ou igual a -90")
        @DecimalMax(value = "90.0", message = "deve ser menor ou igual a 90")
        @Schema(example = "-23.5505")
        Double latitude,

        @DecimalMin(value = "-180.0", message = "deve ser maior ou igual a -180")
        @DecimalMax(value = "180.0", message = "deve ser menor ou igual a 180")
        @Schema(example = "-46.6333")
        Double longitude,

        @Size(max = 100, message = "deve ter no máximo 100 caracteres")
        @Schema(example = "São Paulo")
        String city
) {
    public String countryCodeOrDefault() {
        return countryCode == null || countryCode.isBlank() ? "BR" : countryCode;
    }
}
