package br.com.guilherme.javabase.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import tools.jackson.databind.ObjectMapper;

import br.com.guilherme.javabase.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Sem este handler, uma requisicao sem token (ou com token invalido) numa rota protegida recebe
 * o 401 generico do Spring Security, sem corpo no formato {@link ErrorResponse} usado pelo resto
 * da API (via {@code GlobalExceptionHandler}, java-base). Aqui devolvemos o mesmo formato.
 */
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public RestAuthenticationEntryPoint(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(final HttpServletRequest request, final HttpServletResponse response,
            final AuthenticationException authException) throws IOException {
        final HttpStatus status = HttpStatus.UNAUTHORIZED;
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(
                ErrorResponse.of(status.value(), status.getReasonPhrase(), "Authentication required", request.getRequestURI())));
    }
}
