package br.com.guilherme.javabase.security;

import java.util.Set;

import br.com.guilherme.javabase.controller.ControllerMethodsCore;
import br.com.guilherme.javabase.exception.ErrorSeverity;
import br.com.guilherme.javabase.exception.EnumErrorMessage;
import br.com.guilherme.javabase.exception.UnauthorizedException;
import br.com.guilherme.javabase.util.SecurityUtils;

/**
 * Implementacao padrao de {@link AccessGuard}: nega o acesso se a role do usuario logado
 * ({@code SecurityUtils.getLoggedUserRole()}) nao estiver entre as {@code allowedRoles}
 * declaradas pelo controller. {@code allowedRoles} vazio nao restringe nada (qualquer usuario
 * autenticado passa) - o mesmo comportamento de antes desta checagem existir.
 */
public class RoleAccessGuard implements AccessGuard {

    @Override
    public void checkAccess(final String resourceCode, final ControllerMethodsCore operation,
            final Set<String> allowedRoles) {
        if (allowedRoles == null || allowedRoles.isEmpty())
            return;

        final String role = SecurityUtils.getLoggedUserRole();
        if (role == null || !allowedRoles.contains(role))
            throw new UnauthorizedException(ErrorSeverity.ERROR, EnumErrorMessage.ERR_INSUFFICIENT_ROLE);
    }
}
