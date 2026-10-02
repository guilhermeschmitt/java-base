package br.com.guilherme.javabase.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import br.com.guilherme.javabase.storage.FileStorage;
import br.com.guilherme.javabase.storage.LocalFileStorage;

/**
 * Registra um {@link LocalFileStorage} quando o projeto configura {@code app.storage.dir} (o diretorio onde os
 * arquivos enviados ficam; no docker, um volume). Um projeto que registre o proprio {@link FileStorage} (ex. S3) tem
 * prioridade; sem a propriedade, nenhum storage e' criado.
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "app.storage", name = "dir")
public class FileStorageAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(FileStorage.class)
    public FileStorage fileStorage(@Value("${app.storage.dir}") final String dir) throws IOException {
        return new LocalFileStorage(dir);
    }
}
