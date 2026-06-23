package br.usp.lab.oo.planejador_feriado.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.UUID;

/**
 * Traduz exceções de domínio e de framework em respostas HTTP consistentes
 * para os endpoints REST ({@code /api/v1/...}).
 *
 * <p>Estende {@link ResponseEntityExceptionHandler} para preservar o tratamento
 * padrão do Spring MVC (ex.: 400 para parâmetros obrigatórios ausentes ou
 * inválidos e o status original de {@code ResponseStatusException}), adicionando
 * mapeamentos específicos do domínio.</p>
 *
 * <p>Exceções não mapeadas (erros inesperados) nunca expõem {@code ex.getMessage()}
 * ao cliente — apenas um {@code traceId} correlacionável com o log do servidor,
 * evitando vazar detalhes internos (stack trace, nomes de classe, SQL etc.).</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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
        String traceId = UUID.randomUUID().toString();
        log.error("Erro inesperado [traceId={}] em {}", traceId, request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno inesperado", request, traceId);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message, HttpServletRequest request) {
        return build(status, message, request, UUID.randomUUID().toString());
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message, HttpServletRequest request, String traceId) {
        ApiError body = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                traceId
        );
        return ResponseEntity.status(status).body(body);
    }
}
