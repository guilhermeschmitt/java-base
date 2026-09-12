package br.com.guilherme.javabase.util;

import org.springframework.cache.CacheManager;

/**
 * Guarda a referencia estatica ao {@link CacheManager} do Spring, registrada por
 * {@code CachingAutoConfiguration} assim que o contexto sobe. Existe porque
 * {@code BaseControllerDispatcherImpl} nao e um bean Spring (e instanciado manualmente por
 * {@code BaseControllerImpl}), entao nao recebe {@code @Autowired} - mesmo motivo por tras do
 * {@link SecurityUtils}.
 *
 * <p>{@link #getCacheManager()} pode devolver {@code null} num contexto que nao subiu o
 * {@code CachingAutoConfiguration} (ex.: um teste que nao carrega o Spring Boot inteiro);
 * quem consome deve tratar esse caso como "cache indisponivel", nao como erro.
 */
public final class CacheHolder {

    private static volatile CacheManager cacheManager;

    private CacheHolder() {
    }

    public static void setCacheManager(final CacheManager manager) {
        cacheManager = manager;
    }

    public static CacheManager getCacheManager() {
        return cacheManager;
    }
}
