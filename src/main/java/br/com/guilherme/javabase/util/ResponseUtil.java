package br.com.guilherme.javabase.util;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Monta as respostas HTTP padronizadas usadas pelos dispatchers/controllers da biblioteca.
 */
public final class ResponseUtil {

    private ResponseUtil() {
    }

    public static <T> ResponseEntity<T> processResponse(final Optional<T> body) {
        return body.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NO_CONTENT).build());
    }

    public static <T> ResponseEntity<T> processResponse(final T body) {
        if (body == null)
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        return ResponseEntity.ok(body);
    }

    public static <T> ResponseEntity<T> processResponsePost(final Optional<T> body) {
        return body.map(b -> ResponseEntity.status(HttpStatus.CREATED).body(b))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.CREATED).build());
    }

    public static <T> ResponseEntity<T> processResponsePut(final Optional<T> body) {
        return body.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.OK).build());
    }

    public static <T> ResponseEntity<T> noContent() {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    public static <T> ResponseEntity<T> ok() {
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    public static <T> ResponseEntity<T> notImplemented() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
