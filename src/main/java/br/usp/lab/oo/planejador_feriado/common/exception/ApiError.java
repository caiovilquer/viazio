package br.usp.lab.oo.planejador_feriado.common.exception;

import java.time.Instant;

/**
 * Corpo padronizado de erro retornado pelos endpoints REST.
 */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        String traceId
) {}
