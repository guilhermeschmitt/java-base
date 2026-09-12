package br.com.guilherme.javabase.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;

import br.com.guilherme.javabase.util.CacheHolder;

/**
 * Registra o {@link CacheManager} do Spring Boot (o mesmo bean que um projeto consumidor
 * configura - Redis, se {@code spring-boot-starter-data-redis} estiver no classpath, ou o
 * {@code ConcurrentMapCacheManager} em memoria como fallback padrao do Boot) no
 * {@link CacheHolder}, para uso em {@code BaseControllerDispatcherImpl} (cache de GET por id).
 *
 * <p>{@code @EnableCaching} e' necessario aqui mesmo sem usar {@code @Cacheable} em lugar
 * nenhum: e' o que faz a auto-configuracao de cache do Boot efetivamente criar o bean
 * {@link CacheManager} (sem ele, {@code CacheAutoConfiguration} do Boot fica inativa).
 */
@AutoConfiguration
@ConditionalOnClass(CacheManager.class)
@EnableCaching
public class CachingAutoConfiguration {

    public CachingAutoConfiguration(final CacheManager cacheManager) {
        CacheHolder.setCacheManager(cacheManager);
    }
}
