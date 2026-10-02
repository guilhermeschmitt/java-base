package br.com.guilherme.javabase.storage;

import java.io.InputStream;

import org.springframework.core.io.Resource;

/**
 * Onde ficam os arquivos enviados pelos usuarios (anexos, midias). O banco do projeto guarda so' a chave devolvida por
 * {@link #store}. A implementacao padrao e' um diretorio ({@link LocalFileStorage}, criada pela
 * {@code FileStorageAutoConfiguration} com {@code app.storage.dir}); trocar por S3/MinIO = outra implementacao
 * registrada como bean, sem mexer em quem usa.
 */
public interface FileStorage {

    /**
     * Grava o conteudo em stream sob uma chave nova (dentro de {@code prefix}), sem nunca ter o arquivo inteiro em
     * memoria. Passou de {@code maxBytes} ou veio vazio = 400 (e nada fica gravado).
     */
    Stored store(String prefix, InputStream content, long maxBytes);

    /** Conteudo de uma chave gravada (lido em stream na resposta). */
    Resource load(String key);

    /** Apaga o arquivo da chave; nao existir mais nao e' erro. */
    void delete(String key);

    record Stored(String key, long size) {
    }
}
