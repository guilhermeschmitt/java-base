package br.com.guilherme.javabase.exception;

import org.springframework.http.HttpStatus;

/**
 * Erro de requisicao invalida (parametro ausente, dado duplicado, filtro malformado, etc).
 */
public class BadRequestException extends DomainException {

    public BadRequestException(final ErrorSeverity severity, final EnumErrorMessage errorMessage) {
        super(severity, errorMessage);
    }

    public BadRequestException(final ErrorSeverity severity, final EnumErrorMessage errorMessage, final String detail) {
        super(severity, errorMessage, errorMessage.getMessage() + ": " + detail);
    }

    @Override
    public int httpStatus() {
        return HttpStatus.BAD_REQUEST.value();
    }
}
