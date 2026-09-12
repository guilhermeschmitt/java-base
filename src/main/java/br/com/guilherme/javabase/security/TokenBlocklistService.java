package br.com.guilherme.javabase.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Estado de sessao em Redis - separado do {@code CacheManager}/{@code RedisCacheManager} usado
 * pelo cache de GET por id (java-base): aqui e' {@link StringRedisTemplate} puro, porque o valor
 * e' sempre uma string simples (id do usuario) ou so' a presenca da chave, sem necessidade da
 * serializacao JSON com hint de tipo daquele outro cache.
 *
 * <p>Refresh token = allowlist: existe em Redis so' enquanto for valido (login/refresh criam a
 * entrada, logout/rotacao apagam). Access token = blocklist: normalmente nao esta em lugar
 * nenhum (a assinatura+expiracao do proprio JWT bastam); so' entra aqui no logout, pra derrubar
 * a validade dele imediatamente em vez de esperar expirar sozinho.
 */
public class TokenBlocklistService {

    private static final String ACCESS_BLOCKLIST_PREFIX = "auth:blocklist:";
    private static final String REFRESH_ALLOWLIST_PREFIX = "auth:refresh:";

    private final StringRedisTemplate redisTemplate;

    public TokenBlocklistService(final StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void blockAccessToken(final String jti, final Instant expiresAt) {
        final Duration ttl = Duration.between(Instant.now(), expiresAt);
        if (ttl.isPositive())
            redisTemplate.opsForValue().set(ACCESS_BLOCKLIST_PREFIX + jti, "1", ttl);
    }

    public boolean isAccessTokenBlocked(final String jti) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(ACCESS_BLOCKLIST_PREFIX + jti));
    }

    public void allowRefreshToken(final String jti, final Long userId, final Instant expiresAt) {
        final Duration ttl = Duration.between(Instant.now(), expiresAt);
        if (ttl.isPositive())
            redisTemplate.opsForValue().set(REFRESH_ALLOWLIST_PREFIX + jti, String.valueOf(userId), ttl);
    }

    /**
     * Vazio se o refresh token nao esta (mais) na allowlist - expirado, ja usado (rotacao) ou
     * revogado no logout. Quem chama deve tratar isso como token invalido, sem diferenciar o
     * motivo pro cliente.
     */
    public Optional<Long> resolveRefreshToken(final String jti) {
        final String value = redisTemplate.opsForValue().get(REFRESH_ALLOWLIST_PREFIX + jti);
        return Optional.ofNullable(value).map(Long::valueOf);
    }

    public void revokeRefreshToken(final String jti) {
        redisTemplate.delete(REFRESH_ALLOWLIST_PREFIX + jti);
    }
}
