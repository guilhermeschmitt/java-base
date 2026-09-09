package br.com.guilherme.javabase.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import br.com.guilherme.javabase.exception.ExceptionDatabaseImpl;

/**
 * Registra {@link ExceptionDatabaseImpl} automaticamente em qualquer projeto que dependa
 * desta biblioteca, sem depender do component-scan da aplicacao consumidora alcancar o
 * pacote {@code br.com.guilherme.javabase} (que, por padrao, ele nao alcanca).
 */
@AutoConfiguration
public class ExceptionDatabaseAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ExceptionDatabaseImpl exceptionDatabaseImpl() {
        return new ExceptionDatabaseImpl();
    }
}
