package br.com.guilherme.javabase.exception;

import org.springframework.http.HttpStatus;

/**
 * Registro nao encontrado pelo ID informado, com o contexto (descricao + ID) que originou a busca.
 */
public class EntityNotFound extends DomainException {

    private final transient Object id;

    public EntityNotFound(final ErrorSeverity severity, final EnumErrorMessage errorMessage, final String description,
            final Object id) {
        super(severity, errorMessage, buildMessage(errorMessage, description, id));
        this.id = id;
    }

    private static String buildMessage(final EnumErrorMessage errorMessage, final String description, final Object id) {
        final StringBuilder message = new StringBuilder(errorMessage.getMessage());
        if (description != null && !description.isBlank())
            message.append(" - ").append(description);
        if (id != null)
            message.append(" (id=").append(id).append(')');
        return message.toString();
    }

    public Object getId() {
        return id;
    }

    @Override
    public int httpStatus() {
        return HttpStatus.NOT_FOUND.value();
    }
}
