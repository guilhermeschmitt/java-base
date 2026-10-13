package br.com.guilherme.javabase.storage;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import br.com.guilherme.javabase.exception.BadRequestException;
import br.com.guilherme.javabase.exception.EnumErrorMessage;
import br.com.guilherme.javabase.exception.ErrorSeverity;

/**
 * Tipo de um arquivo enviado pelos primeiros bytes ("magic numbers") - o tipo declarado pelo navegador nao vale. Cada
 * uso passa os tipos que aceita ({@link #detect}); os reconhecidos estao nas constantes. Tambem limpa o nome com que
 * o arquivo vai ser mostrado/baixado ({@link #cleanName}).
 */
public final class FileTypes {

    public static final String PDF = "application/pdf";
    public static final String PNG = "image/png";
    public static final String JPEG = "image/jpeg";
    public static final String GIF = "image/gif";
    public static final String WEBP = "image/webp";
    public static final String MP4 = "video/mp4";
    public static final String WEBM = "video/webm";
    public static final String DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    public static final String XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    public static final String PPTX = "application/vnd.openxmlformats-officedocument.presentationml.presentation";

    /** Extensoes validas de cada tipo (a primeira entra no nome quando ele nao tem nenhuma delas). */
    private static final Map<String, List<String>> EXTENSIONS = Map.of(
            PDF, List.of("pdf"),
            PNG, List.of("png"),
            JPEG, List.of("jpg", "jpeg"),
            GIF, List.of("gif"),
            WEBP, List.of("webp"),
            MP4, List.of("mp4", "mov"),
            WEBM, List.of("webm"),
            DOCX, List.of("docx"),
            XLSX, List.of("xlsx"),
            PPTX, List.of("pptx"));

    // Office (OOXML) e' um ZIP: o tipo sai da pasta das entradas (word/, xl/, ppt/), que aparecem nos primeiros KB.
    private static final Set<String> OFFICE = Set.of(DOCX, XLSX, PPTX);
    private static final byte[] ZIP_ENTRY = { 'P', 'K', 3, 4 };
    private static final int ZIP_SCAN_SIZE = 64 * 1024;

    private static final int HEADER_SIZE = 16;
    private static final int NAME_MAX = 255;

    private FileTypes() {
    }

    /**
     * Tipo do conteudo entre os {@code allowed}, sem consumir o stream (marca e volta). Vazio quando nao e' nenhum
     * deles; conteudo vazio = 400.
     */
    public static Optional<String> detect(final BufferedInputStream content, final Set<String> allowed) {
        final boolean office = allowed.stream().anyMatch(OFFICE::contains);
        final byte[] header;
        try {
            content.mark(office ? ZIP_SCAN_SIZE : HEADER_SIZE);
            final byte[] start = content.readNBytes(HEADER_SIZE);
            header = office && startsWith(start, 0, ZIP_ENTRY)
                    ? concat(start, content.readNBytes(ZIP_SCAN_SIZE - HEADER_SIZE))
                    : start;
            content.reset();
        } catch (final IOException e) {
            throw new UncheckedIOException("could not read the upload", e);
        }
        if (header.length == 0)
            throw new BadRequestException(ErrorSeverity.ERROR, EnumErrorMessage.ERR_NOT_ALLOWED,
                    "file must not be empty");
        return Optional.ofNullable(typeOf(header)).filter(allowed::contains);
    }

    /**
     * Nome pra mostrar/baixar: sem caminho nem caracteres de controle, ate' 255 caracteres (cortando antes da
     * extensao) e com uma extensao que bate com o tipo detectado (senao, a do tipo e' acrescentada).
     */
    public static String cleanName(final String raw, final String contentType) {
        String name = raw == null ? "" : raw;
        name = name.substring(Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\')) + 1)
                .replaceAll("\\p{Cntrl}", "")
                .strip();
        final List<String> extensions = EXTENSIONS.getOrDefault(contentType, List.of("bin"));
        final int dot = name.lastIndexOf('.');
        final String extension = dot > 0 ? name.substring(dot + 1).toLowerCase(Locale.ROOT) : "";
        String base = extensions.contains(extension) ? name.substring(0, dot) : name;
        final String suffix = "." + (extensions.contains(extension) ? extension : extensions.get(0));
        if (base.isBlank())
            base = "file";
        if (base.length() + suffix.length() > NAME_MAX)
            base = base.substring(0, NAME_MAX - suffix.length()).strip();
        return base + suffix;
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
        if (startsWith(header, 0, ZIP_ENTRY))
            return officeType(header);
        return null;
    }

    /** Percorre os cabecalhos locais do ZIP lidos (nome da entrada no offset 30, tamanho dele no 26). */
    private static String officeType(final byte[] data) {
        for (int at = 0; at + 30 <= data.length; at++) {
            if (!startsWith(data, at, ZIP_ENTRY))
                continue;
            final int nameLength = (data[at + 26] & 0xFF) | (data[at + 27] & 0xFF) << 8;
            if (at + 30 + nameLength > data.length)
                break;
            final String name = new String(data, at + 30, nameLength, StandardCharsets.UTF_8);
            if (name.startsWith("word/"))
                return DOCX;
            if (name.startsWith("xl/"))
                return XLSX;
            if (name.startsWith("ppt/"))
                return PPTX;
        }
        return null;
    }

    private static byte[] concat(final byte[] first, final byte[] second) {
        final byte[] all = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, all, first.length, second.length);
        return all;
    }

    private static byte[] ascii(final String value) {
        return value.getBytes(StandardCharsets.US_ASCII);
    }

    private static boolean startsWith(final byte[] data, final int offset, final byte[] prefix) {
        return data.length >= offset + prefix.length
                && Arrays.equals(data, offset, offset + prefix.length, prefix, 0, prefix.length);
    }
}
