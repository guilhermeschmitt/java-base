package br.com.guilherme.javabase.exception;

/**
 * Classifica a severidade de um erro de negocio, para uso em logs e no payload de erro
 * retornado ao cliente pelo {@link GlobalExceptionHandler}.
 */
public enum ErrorSeverity {
    WARNING,
    ERROR,
    FATAL
}
