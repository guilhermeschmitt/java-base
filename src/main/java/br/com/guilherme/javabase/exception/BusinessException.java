package br.com.guilherme.javabase.exception;

/**
 * Base para excecoes de regra de negocio. Cada projeto que consome esta biblioteca deve
 * estender esta classe para os seus proprios erros de dominio, informando o status HTTP
 * correspondente para que o {@link GlobalExceptionHandler} traduza a excecao automaticamente.
 */
public abstract class BusinessException extends RuntimeException {

    protected BusinessException(String message) {
        super(message);
    }

    protected BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public abstract int httpStatus();
}
