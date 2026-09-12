package br.com.guilherme.javabase.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Emite e valida os JWT de acesso e de refresh. Ambos sao assinados com a mesma chave HMAC
 * ({@code app.jwt.secret}) mas tem TTLs diferentes e claims diferentes - o token de acesso carrega
 * {@code clientId}/{@code role} (pro filtro popular {@code CurrentUserHolder} sem consultar o
 * banco a cada requisicao); o de refresh so carrega o id do usuario, porque o projeto consumidor
 * deve sempre conferir o usuario de novo no banco antes de emitir um par novo (rejeita usuario
 * inativo/removido mesmo com um refresh token ainda "valido").
 *
 * <p>Generico de proposito - nao conhece a entidade {@code User} (ou equivalente) do projeto
 * consumidor, so' os dados primitivos necessarios pro token.
 */
public class JwtService {

    private static final String CLAIM_CLIENT_ID = "clientId";
    private static final String CLAIM_ROLE = "role";

    private final SecretKey key;
    private final JwtProperties properties;

    public JwtService(final JwtProperties properties) {
        this.key = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
        this.properties = properties;
    }

    public GeneratedToken generateAccessToken(final Long userId, final Long clientId, final String role) {
        final Instant now = Instant.now();
        final Instant expiresAt = now.plus(properties.getAccessTokenTtl());
        final String jti = UUID.randomUUID().toString();

        final String token = Jwts.builder()
                .id(jti)
                .subject(String.valueOf(userId))
                .claim(CLAIM_CLIENT_ID, clientId)
                .claim(CLAIM_ROLE, role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(key)
                .compact();

        return new GeneratedToken(token, jti, expiresAt);
    }

    public GeneratedToken generateRefreshToken(final Long userId) {
        final Instant now = Instant.now();
        final Instant expiresAt = now.plus(properties.getRefreshTokenTtl());
        final String jti = UUID.randomUUID().toString();

        final String token = Jwts.builder()
                .id(jti)
                .subject(String.valueOf(userId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(key)
                .compact();

        return new GeneratedToken(token, jti, expiresAt);
    }

    /**
     * @throws io.jsonwebtoken.JwtException se a assinatura nao bater, ou o token estiver
     *                                       malformado/expirado - quem chama deve tratar como
     *                                       "token invalido", sem detalhar o motivo pro cliente.
     */
    public Claims parseAndValidate(final String token) {
        final Jws<Claims> jws = Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
        return jws.getPayload();
    }

    /**
     * Le como {@code Number} e converte, em vez de {@code claims.get(key, Long.class)}: o JSON
     * nao distingue Integer de Long, entao valores pequenos de clientId podem desserializar como
     * Integer e um cast direto pra Long quebraria.
     */
    public Long getClientId(final Claims claims) {
        final Object value = claims.get(CLAIM_CLIENT_ID);
        return value instanceof final Number number ? number.longValue() : null;
    }

    public String getRole(final Claims claims) {
        return claims.get(CLAIM_ROLE, String.class);
    }

    public record GeneratedToken(String token, String jti, Instant expiresAt) {
    }
}
