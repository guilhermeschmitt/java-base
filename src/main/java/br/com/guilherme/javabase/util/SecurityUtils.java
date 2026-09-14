package br.com.guilherme.javabase.util;

import java.util.function.Supplier;

/**
 * Ponto de extensao para obter o ID do usuario autenticado, usado pelo auditoria automatica
 * de {@code SimpleAuditory}. Esta biblioteca nao depende do Spring Security, entao cada projeto
 * que a consome deve registrar como resolver o usuario logado chamando
 * {@link #setLoggedUserIdSupplier(Supplier)} durante a inicializacao (ex.: em um {@code @Bean}
 * ou {@code @PostConstruct}).
 */
public final class SecurityUtils {

    private static volatile Supplier<Long> loggedUserIdSupplier = () -> null;
    private static volatile Supplier<Long> loggedClientIdSupplier = () -> null;
    private static volatile Supplier<String> loggedUserRoleSupplier = () -> null;

    private SecurityUtils() {
    }

    public static void setLoggedUserIdSupplier(final Supplier<Long> supplier) {
        loggedUserIdSupplier = supplier != null ? supplier : () -> null;
    }

    public static Long getLoggedUserId() {
        return loggedUserIdSupplier.get();
    }

    /**
     * Registrado pelo projeto consumidor da mesma forma que {@link #setLoggedUserIdSupplier}.
     * Usado por {@code BaseServiceImpl} para bloquear CRUD entre clientes diferentes em
     * entidades {@code ClientBaseId}: quando o supplier devolve {@code null} (nenhum contexto de
     * cliente resolvido para a requisicao atual), nenhuma restricao e aplicada.
     */
    public static void setLoggedClientIdSupplier(final Supplier<Long> supplier) {
        loggedClientIdSupplier = supplier != null ? supplier : () -> null;
    }

    public static Long getLoggedClientId() {
        return loggedClientIdSupplier.get();
    }

    /**
     * Registrado da mesma forma que os suppliers acima. Usado por {@code AccessGuard}
     * (java-base) para checar a role do usuario logado nas rotas CRUD genericas de
     * {@code BaseControllerImpl}.
     */
    public static void setLoggedUserRoleSupplier(final Supplier<String> supplier) {
        loggedUserRoleSupplier = supplier != null ? supplier : () -> null;
    }

    public static String getLoggedUserRole() {
        return loggedUserRoleSupplier.get();
    }
}
