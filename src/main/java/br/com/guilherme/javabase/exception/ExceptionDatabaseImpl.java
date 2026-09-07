package br.com.guilherme.javabase.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;

/**
 * Traduz excecoes lancadas pela camada de persistencia em excecoes de negocio conhecidas,
 * para que o {@link GlobalExceptionHandler} sempre devolva uma resposta consistente.
 */
@Component
public class ExceptionDatabaseImpl {

    public RuntimeException translateMessageDatabase(final Exception e) {
        if (e instanceof final BusinessException businessException)
            return businessException;

        if (e instanceof final DataIntegrityViolationException integrityViolation) {
            final Throwable cause = integrityViolation.getMostSpecificCause();
            return new BadRequestException(ErrorSeverity.ERROR, EnumErrorMessage.ERR_RECORD_DOES_NOT_ALLOW,
                    cause != null ? cause.getMessage() : integrityViolation.getMessage());
        }

        if (e instanceof OptimisticLockingFailureException)
            return new UpdateError(ErrorSeverity.ERROR, EnumErrorMessage.ERR_EDIT_NOT_ALLOWED_IN_CONTEXT);

        return new RuntimeException(e.getMessage(), e);
    }
}
