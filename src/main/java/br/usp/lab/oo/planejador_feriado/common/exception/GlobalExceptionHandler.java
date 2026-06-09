package br.usp.lab.oo.planejador_feriado.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;

/**
 * Traduz exceções de domínio e de framework em respostas HTTP consistentes
 * para os endpoints REST ({@code /api/...}).
 *
 * <p>Estende {@link ResponseEntityExceptionHandler} para preservar o tratamento
 * padrão do Spring MVC (ex.: 400 para parâmetros obrigatórios ausentes ou
 * inválidos e o status original de {@code ResponseStatusException}), adicionando
 * mapeamentos específicos do domínio.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<ApiError> handleExternalApi(ExternalApiException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_GATEWAY, ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest request) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Erro interno inesperado";
        return build(HttpStatus.INTERNAL_SERVER_ERROR, message, request);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message, HttpServletRequest request) {
        ApiError body = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(body);
    }
}
