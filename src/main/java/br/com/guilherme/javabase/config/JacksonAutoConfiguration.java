package br.com.guilherme.javabase.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.cfg.DateTimeFeature;

/**
 * Padroniza a serializacao de datas (ISO-8601 em vez de timestamp numerico) em todos os
 * projetos que dependem desta biblioteca.
 */
@AutoConfiguration
@ConditionalOnClass(JsonMapperBuilderCustomizer.class)
public class JacksonAutoConfiguration {

    @Bean
    public JsonMapperBuilderCustomizer javaBackJsonCustomizer() {
        return builder -> builder.disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
