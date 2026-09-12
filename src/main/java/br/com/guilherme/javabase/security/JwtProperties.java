package br.com.guilherme.javabase.security;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuracao de JWT do projeto consumidor, lida de {@code app.jwt.*} (application.yml). Cada
 * microservico define seu proprio {@code secret} (minimo 32 bytes para HS256) e TTLs via
 * variaveis de ambiente - nada aqui e' compartilhado entre servicos por padrao.
 */
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private String secret;
    private Duration accessTokenTtl;
    private Duration refreshTokenTtl;
    private Duration passwordResetTtl;

    public String getSecret() {
        return secret;
    }

    public void setSecret(final String secret) {
        this.secret = secret;
    }

    public Duration getAccessTokenTtl() {
        return accessTokenTtl;
    }

    public void setAccessTokenTtl(final Duration accessTokenTtl) {
        this.accessTokenTtl = accessTokenTtl;
    }

    public Duration getRefreshTokenTtl() {
        return refreshTokenTtl;
    }

    public void setRefreshTokenTtl(final Duration refreshTokenTtl) {
        this.refreshTokenTtl = refreshTokenTtl;
    }

    public Duration getPasswordResetTtl() {
        return passwordResetTtl;
    }

    public void setPasswordResetTtl(final Duration passwordResetTtl) {
        this.passwordResetTtl = passwordResetTtl;
    }
}
