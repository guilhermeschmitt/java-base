package br.com.guilherme.javabase.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import tools.jackson.databind.ObjectMapper;

import br.com.guilherme.javabase.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Equivalente ao {@link RestAuthenticationEntryPoint}, mas pro caso em que a requisicao esta
 * autenticada (token valido) e ainda assim e' barrada por uma regra de autorizacao (ex.:
 * {@code @PreAuthorize("hasRole(...)")} que a role do usuario nao atende) - 403, no mesmo formato
 * de {@link ErrorResponse}.
 */
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public RestAccessDeniedHandler(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(final HttpServletRequest request, final HttpServletResponse response,
            final AccessDeniedException accessDeniedException) throws IOException {
        final HttpStatus status = HttpStatus.FORBIDDEN;
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(
                ErrorResponse.of(status.value(), status.getReasonPhrase(), "Access denied", request.getRequestURI())));
    }
}
