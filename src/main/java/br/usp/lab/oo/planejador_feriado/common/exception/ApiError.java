package br.usp.lab.oo.planejador_feriado.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Corpo padronizado de erro retornado pelos endpoints REST.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String code,
        String message,
        String path,
        String traceId,
        List<ApiViolation> violations
) {
    public ApiError {
        violations = violations != null ? List.copyOf(violations) : List.of();
    }
}
