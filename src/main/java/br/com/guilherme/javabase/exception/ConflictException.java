package br.com.guilherme.javabase.exception;

import org.springframework.http.HttpStatus;

/**
 * O registro mudou de situacao e a operacao nao cabe mais (ex.: outro usuario pegou a tarefa antes) - 409,
 * com um detalhe do que aconteceu. Para conflito de versao do JPA use {@link UpdateError}.
 */
public class ConflictException extends DomainException {

    public ConflictException(final ErrorSeverity severity, final EnumErrorMessage errorMessage, final String detail) {
        super(severity, errorMessage, errorMessage.getMessage() + ": " + detail);
    }

    @Override
    public int httpStatus() {
        return HttpStatus.CONFLICT.value();
    }
}
