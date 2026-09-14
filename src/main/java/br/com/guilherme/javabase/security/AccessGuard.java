package br.com.guilherme.javabase.security;

import java.util.Set;

import br.com.guilherme.javabase.controller.BaseControllerImpl;
import br.com.guilherme.javabase.controller.ControllerMethodsCore;

/**
 * Ponto de extensao para a checagem de autorizacao nas rotas CRUD genericas de
 * {@link BaseControllerImpl}. A implementacao padrao ({@link RoleAccessGuard}, registrada por
 * {@code SecurityAutoConfiguration}) checa so' a role do usuario logado contra
 * {@code getAllowedRolesCore()} do controller.
 *
 * <p>{@code resourceCode} existe desde ja' para permitir, no futuro, uma implementacao mais
 * granular (por pagina/acao, no estilo Profile/SystemPage/PageAction) sem precisar mudar a
 * assinatura nem os controllers - um projeto consumidor troca o comportamento inteiro do sistema
 * so' registrando seu proprio bean {@code AccessGuard} (a implementacao padrao usa
 * {@code @ConditionalOnMissingBean}).
 */
public interface AccessGuard {

    /**
     * @param resourceCode identificador do recurso sendo acessado (ex.: {@code getResourceCode()}
     *                      do controller) - ignorado pela implementacao padrao, disponivel para
     *                      implementacoes futuras mais granulares.
     * @param operation     qual operacao CRUD esta sendo executada.
     * @param allowedRoles  roles declaradas pelo controller para esta operacao; vazio significa
     *                      "sem restricao alem de estar autenticado".
     * @throws br.com.guilherme.javabase.exception.UnauthorizedException se o acesso for negado.
     */
    void checkAccess(String resourceCode, ControllerMethodsCore operation, Set<String> allowedRoles);
}
