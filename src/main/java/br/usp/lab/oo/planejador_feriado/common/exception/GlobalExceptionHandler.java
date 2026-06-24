package br.usp.lab.oo.planejador_feriado.common.exception;

import br.usp.lab.oo.planejador_feriado.common.trace.RequestTraceFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Traduz exceções de domínio e de framework em respostas HTTP consistentes
 * para os endpoints REST ({@code /api/v1/...}).
 *
 * <p>Estende {@link ResponseEntityExceptionHandler} para preservar os status do
 * Spring MVC e normalizar também seus erros no mesmo envelope {@link ApiError}
 * usado pelas exceções de domínio.</p>
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
        return build(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<ApiError> handleExternalApi(ExternalApiException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_GATEWAY, "EXTERNAL_API_ERROR", ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleInvalidArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest request) {
        String traceId = traceId(request);
        log.error("Erro inesperado [traceId={}] em {}", traceId, request.getRequestURI(), ex);
        return build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR",
                "Erro interno inesperado",
                request.getRequestURI(),
                traceId,
                List.of());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        List<ApiViolation> violations = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ApiViolation(
                        error.getField(),
                        error.getDefaultMessage() != null ? error.getDefaultMessage() : "valor inválido"))
                .toList();
        ApiError body = error(
                status,
                "VALIDATION_ERROR",
                "A requisição contém campos inválidos",
                path(request),
                traceId(request),
                violations);
        return new ResponseEntity<>(body, jsonHeaders(headers), status);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex,
            Object body,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        String traceId = traceId(request);
        String message = frameworkMessage(ex, body, status);
        String code = frameworkCode(ex, status);
        if (status.is5xxServerError()) {
            log.error("Erro HTTP inesperado [traceId={}] em {}", traceId, path(request), ex);
            message = "Erro interno inesperado";
        }
        return new ResponseEntity<>(
                error(status, code, message, path(request), traceId, List.of()),
                jsonHeaders(headers),
                status);
    }

    private String frameworkMessage(Exception ex, Object body, HttpStatusCode status) {
        if (ex instanceof ResponseStatusException responseStatus
                && responseStatus.getReason() != null
                && !responseStatus.getReason().isBlank()) {
            return responseStatus.getReason();
        }
        if (body instanceof ProblemDetail problem
                && problem.getDetail() != null
                && !problem.getDetail().isBlank()) {
            return problem.getDetail();
        }
        if (ex instanceof ErrorResponse errorResponse
                && errorResponse.getBody().getDetail() != null
                && !errorResponse.getBody().getDetail().isBlank()) {
            return errorResponse.getBody().getDetail();
        }
        return HttpStatus.valueOf(status.value()).getReasonPhrase();
    }

    private String frameworkCode(Exception ex, HttpStatusCode status) {
        if (ex instanceof org.springframework.http.converter.HttpMessageNotReadableException) {
            return "MALFORMED_REQUEST";
        }
        if (status.value() == HttpStatus.NOT_FOUND.value()) {
            return "RESOURCE_NOT_FOUND";
        }
        return status.is4xxClientError() ? "INVALID_REQUEST" : "INTERNAL_ERROR";
    }

    private String path(WebRequest request) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            return servletWebRequest.getRequest().getRequestURI();
        }
        return request.getDescription(false).replace("uri=", "");
    }

    private HttpHeaders jsonHeaders(HttpHeaders source) {
        HttpHeaders headers = new HttpHeaders();
        headers.putAll(source);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private ResponseEntity<ApiError> build(
            HttpStatus status,
            String code,
            String message,
            HttpServletRequest request,
            List<ApiViolation> violations) {
        return build(status, code, message, request.getRequestURI(), traceId(request), violations);
    }

    private ResponseEntity<ApiError> build(
            HttpStatus status,
            String code,
            String message,
            String path,
            String traceId,
            List<ApiViolation> violations) {
        return ResponseEntity.status(status).body(error(status, code, message, path, traceId, violations));
    }

    private ApiError error(
            HttpStatusCode status,
            String code,
            String message,
            String path,
            String traceId,
            List<ApiViolation> violations) {
        HttpStatus httpStatus = HttpStatus.valueOf(status.value());
        return new ApiError(
                Instant.now(),
                httpStatus.value(),
                httpStatus.getReasonPhrase(),
                code,
                message,
                path,
                traceId,
                violations
        );
    }

    private String traceId(HttpServletRequest request) {
        Object value = request.getAttribute(RequestTraceFilter.ATTRIBUTE);
        return value instanceof String traceId && !traceId.isBlank()
                ? traceId
                : UUID.randomUUID().toString();
    }

    private String traceId(WebRequest request) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            return traceId(servletWebRequest.getRequest());
        }
        return UUID.randomUUID().toString();
    }
}
