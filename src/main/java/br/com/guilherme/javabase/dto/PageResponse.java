package br.com.guilherme.javabase.dto;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 * Envelope de paginacao desacoplado de {@link Page}, para nao expor tipos do Spring Data
 * diretamente nas respostas de API.
 */
public record PageResponse<T>(
        List<T> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean last
) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
