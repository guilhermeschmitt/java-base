package br.com.guilherme.javabase.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import br.com.guilherme.javabase.dto.EnumOptionDto;
import br.com.guilherme.javabase.dto.LabeledEnum;

/**
 * Controller somente-leitura para expor as constantes de um enum (que implemente
 * {@link LabeledEnum}) como opcoes {@code {label, value}} paginadas, no mesmo formato de
 * {@code Page<B>} devolvido por {@code BaseControllerImpl#getObjectList}. Nao herda de
 * {@link BaseControllerImpl} porque nao ha entidade/repositorio/converter por tras - a "fonte de
 * dados" e' {@code enumClass.getEnumConstants()}, fixa em memoria.
 */
public abstract class BaseEnumControllerImpl<E extends Enum<E> & LabeledEnum> {

    private final Class<E> enumClass;

    protected BaseEnumControllerImpl(final Class<E> enumClass) {
        this.enumClass = enumClass;
    }

    @GetMapping
    public ResponseEntity<Page<EnumOptionDto>> getObjectList(
            @PageableDefault(page = 0, size = 10, sort = "value", direction = Direction.ASC) final Pageable page) {
        final List<EnumOptionDto> options = Arrays.stream(enumClass.getEnumConstants())
                .map(e -> new EnumOptionDto(e.getLabel(), e.name()))
                .toList();

        final int start = Math.min((int) page.getOffset(), options.size());
        final int end = Math.min(start + page.getPageSize(), options.size());

        return ResponseEntity.ok(new PageImpl<>(options.subList(start, end), page, options.size()));
    }
}
