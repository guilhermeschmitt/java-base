package br.com.guilherme.javabase.storage;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import br.com.guilherme.javabase.exception.BadRequestException;
import br.com.guilherme.javabase.exception.EnumErrorMessage;
import br.com.guilherme.javabase.exception.ErrorSeverity;

/**
 * Tipo de um arquivo enviado pelos primeiros bytes ("magic numbers") - o tipo declarado pelo navegador nao vale. Cada
 * uso passa os tipos que aceita ({@link #detect}); os reconhecidos estao nas constantes.
 */
public final class FileTypes {

    public static final String PDF = "application/pdf";
    public static final String PNG = "image/png";
    public static final String JPEG = "image/jpeg";
    public static final String GIF = "image/gif";
    public static final String WEBP = "image/webp";
    public static final String MP4 = "video/mp4";
    public static final String WEBM = "video/webm";

    private static final int HEADER_SIZE = 16;

    private FileTypes() {
    }

    /**
     * Tipo do conteudo entre os {@code allowed}, sem consumir o stream (marca e volta). Vazio quando nao e' nenhum
     * deles; conteudo vazio = 400.
     */
    public static Optional<String> detect(final BufferedInputStream content, final Set<String> allowed) {
        final byte[] header;
        try {
            content.mark(HEADER_SIZE);
            header = content.readNBytes(HEADER_SIZE);
            content.reset();
        } catch (final IOException e) {
            throw new UncheckedIOException("could not read the upload", e);
        }
        if (header.length == 0)
            throw new BadRequestException(ErrorSeverity.ERROR, EnumErrorMessage.ERR_NOT_ALLOWED,
                    "file must not be empty");
        return Optional.ofNullable(typeOf(header)).filter(allowed::contains);
    }

    private static String typeOf(final byte[] header) {
        if (startsWith(header, 0, ascii("%PDF-")))
            return PDF;
        if (startsWith(header, 0, new byte[] { (byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n' }))
            return PNG;
        if (startsWith(header, 0, new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF }))
            return JPEG;
        if (startsWith(header, 0, ascii("GIF87a")) || startsWith(header, 0, ascii("GIF89a")))
            return GIF;
        if (startsWith(header, 0, ascii("RIFF")) && startsWith(header, 8, ascii("WEBP")))
            return WEBP;
        // MP4/MOV: caixa "ftyp" logo apos o tamanho (4 bytes).
        if (startsWith(header, 4, ascii("ftyp")))
            return MP4;
        // WebM/Matroska: cabecalho EBML.
        if (startsWith(header, 0, new byte[] { 0x1A, 0x45, (byte) 0xDF, (byte) 0xA3 }))
            return WEBM;
        return null;
    }

    private static byte[] ascii(final String value) {
        return value.getBytes(StandardCharsets.US_ASCII);
    }

    private static boolean startsWith(final byte[] data, final int offset, final byte[] prefix) {
        return data.length >= offset + prefix.length
                && Arrays.equals(data, offset, offset + prefix.length, prefix, 0, prefix.length);
    }
}
