package br.usp.lab.oo.planejador_feriado.common.exception;

/**
 * Lançada quando um recurso solicitado (país, região, câmbio) não existe.
 * Mapeada para HTTP 404 no {@code GlobalExceptionHandler}.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
