package br.com.guilherme.javabase.exception;

import org.springframework.http.HttpStatus;

/**
 * Identidade nao estabelecida ou nao confirmada (ex.: credenciais invalidas no login, token
 * ausente/invalido/expirado/revogado). Distinta de {@link UnauthorizedException} (403): aqui
 * ainda nao sabemos quem esta fazendo a requisicao (ou a prova de identidade falhou); la ja
 * sabemos quem e, mas a acao nao e permitida pra essa identidade.
 */
public class UnauthenticatedException extends DomainException {

    public UnauthenticatedException(final ErrorSeverity severity, final EnumErrorMessage errorMessage) {
        super(severity, errorMessage);
    }

    @Override
    public int httpStatus() {
        return HttpStatus.UNAUTHORIZED.value();
    }
}
