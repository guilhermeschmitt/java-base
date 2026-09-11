package br.com.guilherme.javabase.config;

import org.hibernate.Session;
import org.springframework.web.servlet.HandlerInterceptor;

import br.com.guilherme.javabase.domain.ClientBaseId;
import br.com.guilherme.javabase.util.SecurityUtils;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Habilita o filtro Hibernate {@value ClientBaseId#CLIENT_FILTER} (declarado em
 * {@link ClientBaseId}) uma unica vez por requisicao, antes do controller rodar, usando
 * {@link SecurityUtils#getLoggedClientId()} como parametro. Dai em diante, qualquer SELECT feito
 * na sessao da requisicao - por qualquer metodo de qualquer repository, nao so os que passam
 * pelo {@code BaseServiceImpl} - so enxerga linhas do cliente resolvido.
 *
 * <p>Precisa rodar depois que o {@code EntityManager} da requisicao ja esta aberto (feito pelo
 * {@code OpenEntityManagerInViewInterceptor} do Spring Boot, condicionado a
 * {@code spring.jpa.open-in-view=true}), senao {@code em.unwrap(Session.class)} pegaria um
 * {@code EntityManager} temporario e descartado, e o filtro nao valeria pro resto da requisicao.
 * {@link ClientFilterAutoConfiguration} garante essa ordem registrando este interceptor com
 * {@code order} explicito maior que o (default, 0) do interceptor do OSIV.
 */
public class ClientFilterInterceptor implements HandlerInterceptor {

    private final EntityManager em;

    public ClientFilterInterceptor(final EntityManager em) {
        this.em = em;
    }

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) {
        final Long loggedClientId = SecurityUtils.getLoggedClientId();

        if (loggedClientId != null) {
            final Session session = em.unwrap(Session.class);
            session.enableFilter(ClientBaseId.CLIENT_FILTER).setParameter("clientId", loggedClientId);
        }

        return true;
    }
}
