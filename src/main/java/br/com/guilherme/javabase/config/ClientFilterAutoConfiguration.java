package br.com.guilherme.javabase.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Registra o {@link ClientFilterInterceptor} automaticamente em qualquer projeto consumidor com
 * Spring MVC no classpath. O {@code order} explicito (maior que o default 0 usado pelo
 * {@code OpenEntityManagerInViewInterceptor} do Spring Boot) garante que este interceptor sempre
 * rode depois que o {@code EntityManager} da requisicao ja esta aberto, nao importa a ordem em
 * que os beans de configuracao sejam processados.
 */
@AutoConfiguration
@ConditionalOnClass(DispatcherServlet.class)
public class ClientFilterAutoConfiguration implements WebMvcConfigurer {

    @PersistenceContext
    private EntityManager em;

    @Bean
    @ConditionalOnMissingBean
    public ClientFilterInterceptor clientFilterInterceptor() {
        return new ClientFilterInterceptor(em);
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(clientFilterInterceptor()).order(Ordered.LOWEST_PRECEDENCE);
    }
}
