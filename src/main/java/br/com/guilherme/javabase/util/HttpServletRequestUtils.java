package br.com.guilherme.javabase.util;

import java.util.Map;
import java.util.Optional;

import org.springframework.web.servlet.HandlerMapping;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Leitura auxiliar de valores da requisicao HTTP, como variaveis de path do Spring MVC.
 */
public final class HttpServletRequestUtils {

    private HttpServletRequestUtils() {
    }

    public static Optional<String> getPathValue(final HttpServletRequest request, final String name) {
        final Object attribute = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (!(attribute instanceof final Map<?, ?> pathVariables))
            return Optional.empty();
        return Optional.ofNullable((String) pathVariables.get(name));
    }
}
