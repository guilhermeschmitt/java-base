package br.com.guilherme.javabase.dto;

import java.time.Instant;

/**
 * Formato padrao de erro retornado pelo {@code GlobalExceptionHandler}.
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {

    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(Instant.now(), status, error, message, path);
    }
}
