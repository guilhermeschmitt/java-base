package br.com.guilherme.javabase.security;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Le o access token do header {@code Authorization: Bearer <token>}, valida (assinatura,
 * expiracao, nao bloqueado) e, se tudo bater, autentica a requisicao: popula tanto o
 * {@code SecurityContextHolder} do Spring Security (pra {@code authenticated()}/{@code @PreAuthorize}
 * no filter chain) quanto {@link CurrentUserHolder} (pra {@code SecurityUtils} do java-base, usado
 * pelo isolamento multi-tenant e pela auditoria).
 *
 * <p>Token ausente, invalido, expirado ou bloqueado simplesmente NAO autentica a requisicao (nunca
 * lanca excecao daqui) - quem decide se isso e' um problema e' a regra de autorizacao do filter
 * chain do projeto consumidor (rota publica segue normal; rota que exige autenticacao cai no
 * {@code AuthenticationEntryPoint} configurado, com 401).
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER_NAME = "Authorization";
    private static final String HEADER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final TokenBlocklistService blocklistService;

    public JwtAuthenticationFilter(final JwtService jwtService, final TokenBlocklistService blocklistService) {
        this.jwtService = jwtService;
        this.blocklistService = blocklistService;
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
            final FilterChain filterChain) throws ServletException, IOException {
        try {
            authenticate(request);
            filterChain.doFilter(request, response);
        } finally {
            CurrentUserHolder.clear();
            SecurityContextHolder.clearContext();
        }
    }

    private void authenticate(final HttpServletRequest request) {
        final String header = request.getHeader(HEADER_NAME);
        if (header == null || !header.startsWith(HEADER_PREFIX))
            return;

        final String token = header.substring(HEADER_PREFIX.length());

        final Claims claims;
        try {
            claims = jwtService.parseAndValidate(token);
        } catch (final JwtException | IllegalArgumentException e) {
            return;
        }

        final String jti = claims.getId();
        if (blocklistService.isAccessTokenBlocked(jti))
            return;

        final Long userId = Long.valueOf(claims.getSubject());
        final Long clientId = jwtService.getClientId(claims);
        final String role = jwtService.getRole(claims);
        final Instant expiresAt = claims.getExpiration().toInstant();

        CurrentUserHolder.set(new CurrentUserHolder.Context(userId, clientId, role, jti, expiresAt));

        final Authentication authentication = new UsernamePasswordAuthenticationToken(userId, null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
