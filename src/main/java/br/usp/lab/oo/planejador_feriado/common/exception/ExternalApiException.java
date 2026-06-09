package br.usp.lab.oo.planejador_feriado.common.exception;

/**
 * Lançada quando uma API externa (RestCountries, Nager.Date, AwesomeAPI) está
 * indisponível ou responde de forma inesperada. Mapeada para HTTP 502 no
 * {@code GlobalExceptionHandler}.
 */
public class ExternalApiException extends RuntimeException {

    public ExternalApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
