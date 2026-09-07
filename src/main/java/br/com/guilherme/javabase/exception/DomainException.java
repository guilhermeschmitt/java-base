package br.com.guilherme.javabase.exception;

/**
 * Base comum para as excecoes de negocio da biblioteca, associando cada erro a uma
 * {@link ErrorSeverity} e a um codigo padronizado de {@link EnumErrorMessage}.
 */
public abstract class DomainException extends BusinessException {

    private final ErrorSeverity severity;
    private final EnumErrorMessage errorMessage;

    protected DomainException(final ErrorSeverity severity, final EnumErrorMessage errorMessage) {
        this(severity, errorMessage, errorMessage.getMessage());
    }

    protected DomainException(final ErrorSeverity severity, final EnumErrorMessage errorMessage, final String message) {
        super(message);
        this.severity = severity;
        this.errorMessage = errorMessage;
    }

    public ErrorSeverity getSeverity() {
        return severity;
    }

    public EnumErrorMessage getErrorMessage() {
        return errorMessage;
    }
}
