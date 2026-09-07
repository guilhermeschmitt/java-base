package br.com.guilherme.javabase.exception;

import org.springframework.http.HttpStatus;

/**
 * Operacao nao permitida no contexto atual (ex.: edicao ou exclusao bloqueada por regra de negocio).
 */
public class UnauthorizedException extends DomainException {

    public UnauthorizedException(final ErrorSeverity severity, final EnumErrorMessage errorMessage) {
        super(severity, errorMessage);
    }

    @Override
    public int httpStatus() {
        return HttpStatus.FORBIDDEN.value();
    }
}
