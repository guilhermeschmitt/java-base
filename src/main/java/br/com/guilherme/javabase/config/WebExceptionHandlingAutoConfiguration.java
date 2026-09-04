package br.com.guilherme.javabase.config;

import br.com.guilherme.javabase.exception.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * Registra o {@link GlobalExceptionHandler} automaticamente em qualquer projeto com o
 * Spring MVC no classpath, sem que ele precise ser declarado manualmente.
 */
@AutoConfiguration
@ConditionalOnClass(DispatcherServlet.class)
public class WebExceptionHandlingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
}
