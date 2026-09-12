package br.com.guilherme.javabase.security;

import java.time.Instant;

/**
 * Guarda os dados do usuario autenticado na requisicao HTTP atual, extraidos do access token e
 * populados por {@link JwtAuthenticationFilter}. {@code SecurityUtils.getLoggedUserId()} e
 * {@code getLoggedClientId()} (java-base) sao registrados por {@code SecurityAutoConfiguration}
 * apontando pra ca.
 *
 * <p>Um {@code ThreadLocal} por requisicao: nunca deve ser lido/escrito fora do ciclo de vida de
 * uma unica requisicao. {@code role} e' uma String generica (nao um enum do projeto consumidor) -
 * cada microservico decide seu proprio conjunto de roles/types.
 */
public final class CurrentUserHolder {

    private static final ThreadLocal<Context> CURRENT = new ThreadLocal<>();

    private CurrentUserHolder() {
    }

    static void set(final Context context) {
        CURRENT.set(context);
    }

    static void clear() {
        CURRENT.remove();
    }

    public static Context get() {
        return CURRENT.get();
    }

    public static Long getUserId() {
        final Context context = CURRENT.get();
        return context != null ? context.userId() : null;
    }

    public static Long getClientId() {
        final Context context = CURRENT.get();
        return context != null ? context.clientId() : null;
    }

    public static String getRole() {
        final Context context = CURRENT.get();
        return context != null ? context.role() : null;
    }

    /**
     * @param jti       id do access token (JWT) atual - usado no logout pra saber o que colocar
     *                  na blocklist.
     * @param expiresAt validade do access token atual - usado pra calcular o TTL da entrada na
     *                  blocklist (nao faz sentido bloquear alem do tempo que ele ja expiraria
     *                  sozinho).
     */
    public record Context(Long userId, Long clientId, String role, String jti, Instant expiresAt) {
    }
}
