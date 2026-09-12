package br.com.guilherme.javabase.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import tools.jackson.databind.ObjectMapper;

import br.com.guilherme.javabase.security.CurrentUserHolder;
import br.com.guilherme.javabase.security.JwtAuthenticationFilter;
import br.com.guilherme.javabase.security.JwtProperties;
import br.com.guilherme.javabase.security.JwtService;
import br.com.guilherme.javabase.security.PasswordResetTokenService;
import br.com.guilherme.javabase.security.RestAccessDeniedHandler;
import br.com.guilherme.javabase.security.RestAuthenticationEntryPoint;
import br.com.guilherme.javabase.security.TokenBlocklistService;
import br.com.guilherme.javabase.util.SecurityUtils;
import jakarta.annotation.PostConstruct;

/**
 * Registra automaticamente, em qualquer projeto consumidor com Spring Security no classpath, as
 * pecas genericas da autenticacao JWT: {@link JwtService}, {@link TokenBlocklistService},
 * {@link PasswordResetTokenService}, {@link JwtAuthenticationFilter} e os handlers JSON de
 * 401/403. O projeto consumidor so' precisa declarar seu proprio bean {@link SecurityFilterChain}
 * usando essas pecas (rotas publicas, CORS, etc. sao especificas de cada servico) - ver
 * {@code SecurityConfig} em java-test como exemplo.
 *
 * <p>Tambem registra aqui os suppliers de {@link SecurityUtils} (userId/clientId) apontando pra
 * {@link CurrentUserHolder}, populado pelo {@link JwtAuthenticationFilter} - centralizado aqui
 * pra que nenhum projeto consumidor precise lembrar de fazer essa ligacao manualmente.
 */
@AutoConfiguration
@ConditionalOnClass(SecurityFilterChain.class)
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityAutoConfiguration {

    @PostConstruct
    public void registerSecurityUtilsSuppliers() {
        SecurityUtils.setLoggedUserIdSupplier(CurrentUserHolder::getUserId);
        SecurityUtils.setLoggedClientIdSupplier(CurrentUserHolder::getClientId);
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtService jwtService(final JwtProperties properties) {
        return new JwtService(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public TokenBlocklistService tokenBlocklistService(final StringRedisTemplate redisTemplate) {
        return new TokenBlocklistService(redisTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public PasswordResetTokenService passwordResetTokenService(final StringRedisTemplate redisTemplate,
            final JwtProperties properties) {
        return new PasswordResetTokenService(redisTemplate, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtAuthenticationFilter jwtAuthenticationFilter(final JwtService jwtService,
            final TokenBlocklistService tokenBlocklistService) {
        return new JwtAuthenticationFilter(jwtService, tokenBlocklistService);
    }

    @Bean
    @ConditionalOnMissingBean
    public RestAuthenticationEntryPoint restAuthenticationEntryPoint(final ObjectMapper objectMapper) {
        return new RestAuthenticationEntryPoint(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public RestAccessDeniedHandler restAccessDeniedHandler(final ObjectMapper objectMapper) {
        return new RestAccessDeniedHandler(objectMapper);
    }
}
