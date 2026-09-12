package br.com.guilherme.javabase.security;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Token de uso unico pra completar a recuperacao de senha (fluxo recover-password ->
 * reset-password). Guardado so' em Redis, com TTL curto - nao precisa de tabela nem de auditoria
 * por enquanto.
 */
public class PasswordResetTokenService {

    private static final String PREFIX = "auth:password-reset:";

    private final StringRedisTemplate redisTemplate;
    private final JwtProperties properties;

    public PasswordResetTokenService(final StringRedisTemplate redisTemplate, final JwtProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    public String generate(final Long userId) {
        final String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(PREFIX + token, String.valueOf(userId), properties.getPasswordResetTtl());
        return token;
    }

    /**
     * Vazio se o token nao existe (nunca existiu, expirou, ou ja foi consumido - {@link #consume}
     * apaga a chave). Quem chama deve tratar isso como "token invalido ou expirado", sem
     * diferenciar o motivo pro cliente.
     */
    public Optional<Long> resolve(final String token) {
        final String value = redisTemplate.opsForValue().get(PREFIX + token);
        return Optional.ofNullable(value).map(Long::valueOf);
    }

    public void consume(final String token) {
        redisTemplate.delete(PREFIX + token);
    }
}
