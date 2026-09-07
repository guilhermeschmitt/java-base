package br.com.guilherme.javabase.exception;

import org.springframework.http.HttpStatus;

/**
 * Falha ao atualizar um registro por inconsistencia dos dados enviados (ex.: ID da URL
 * diferente do ID do corpo da requisicao).
 */
public class UpdateError extends DomainException {

    public UpdateError(final ErrorSeverity severity, final EnumErrorMessage errorMessage) {
        super(severity, errorMessage);
    }

    @Override
    public int httpStatus() {
        return HttpStatus.CONFLICT.value();
    }
}
