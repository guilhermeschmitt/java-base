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

    private SecurityUtils() {
    }

    public static void setLoggedUserIdSupplier(final Supplier<Long> supplier) {
        loggedUserIdSupplier = supplier != null ? supplier : () -> null;
    }

    public static Long getLoggedUserId() {
        return loggedUserIdSupplier.get();
    }
}
