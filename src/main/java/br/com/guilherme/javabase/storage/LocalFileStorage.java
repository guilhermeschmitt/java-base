package br.com.guilherme.javabase.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import br.com.guilherme.javabase.exception.BadRequestException;
import br.com.guilherme.javabase.exception.EntityNotFound;
import br.com.guilherme.javabase.exception.EnumErrorMessage;
import br.com.guilherme.javabase.exception.ErrorSeverity;

/**
 * {@link FileStorage} num diretorio ({@code app.storage.dir}; no docker, um volume do projeto). A chave e'
 * {@code <prefix>/<uuid>} - o nome original nunca vira caminho. Grava num {@code .part} ao lado e so' move pro nome
 * final quando terminou dentro do limite, entao um upload interrompido nao deixa arquivo pela metade com chave valida.
 */
public class LocalFileStorage implements FileStorage {

    private static final Logger LOG = LoggerFactory.getLogger(LocalFileStorage.class);
    private static final int BUFFER_SIZE = 64 * 1024;

    private final Path root;

    public LocalFileStorage(final String dir) throws IOException {
        this.root = Path.of(dir).toAbsolutePath().normalize();
        Files.createDirectories(root);
    }

    @Override
    public Stored store(final String prefix, final InputStream content, final long maxBytes) {
        final String key = prefix + "/" + UUID.randomUUID();
        final Path target = resolve(key);
        Path part = null;
        try {
            Files.createDirectories(target.getParent());
            part = Files.createTempFile(target.getParent(), "upload-", ".part");
            final long size = copy(content, part, maxBytes);
            if (size == 0)
                throw new BadRequestException(ErrorSeverity.ERROR, EnumErrorMessage.ERR_NOT_ALLOWED,
                        "file must not be empty");
            Files.move(part, target, StandardCopyOption.ATOMIC_MOVE);
            return new Stored(key, size);
        } catch (final IOException e) {
            throw new UncheckedIOException("could not store the file", e);
        } finally {
            deleteQuietly(part);
        }
    }

    @Override
    public Resource load(final String key) {
        final Path path = resolve(key);
        if (!Files.isRegularFile(path))
            throw new EntityNotFound(ErrorSeverity.ERROR, EnumErrorMessage.ERR_RECORD_NOT_EXIST, "file", key);
        return new FileSystemResource(path);
    }

    @Override
    public void delete(final String key) {
        deleteQuietly(resolve(key));
    }

    // Copia contando os bytes: passou do limite, para na hora (nao le o resto) - 400.
    private static long copy(final InputStream content, final Path part, final long maxBytes) throws IOException {
        long total = 0;
        try (OutputStream out = Files.newOutputStream(part)) {
            final byte[] buffer = new byte[BUFFER_SIZE];
            int read;
            while ((read = content.read(buffer)) != -1) {
                total += read;
                if (total > maxBytes)
                    throw new BadRequestException(ErrorSeverity.ERROR, EnumErrorMessage.ERR_NOT_ALLOWED,
                            "file must be at most " + maxBytes / (1024 * 1024) + "MB");
                out.write(buffer, 0, read);
            }
        }
        return total;
    }

    private Path resolve(final String key) {
        final Path path = root.resolve(key).normalize();
        if (!path.startsWith(root))
            throw new IllegalArgumentException("invalid storage key: " + key);
        return path;
    }

    private static void deleteQuietly(final Path path) {
        if (path == null)
            return;
        try {
            Files.deleteIfExists(path);
        } catch (final IOException e) {
            LOG.warn("could not delete {}", path, e);
        }
    }
}
