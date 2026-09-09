package br.com.guilherme.javabase.util;

import java.io.Serializable;

import br.com.guilherme.javabase.domain.BaseEntityInterface;
import br.com.guilherme.javabase.dto.BaseDto;

/**
 * Copia os campos comuns a todo DTO (herdados de {@link BaseDto}) a partir da entidade de origem.
 */
public final class ConverterUtils {

    private ConverterUtils() {
    }

    public static <I extends Serializable, E extends BaseEntityInterface<I>> void defaultValuesEntityToDto(
            final BaseDto<I> dto, final E ent) {
        dto.setId(ent.getId());
        dto.setCreatedAt(ent.getCreatedAt());
        dto.setActive(ent.getActive());
    }

    public static <I extends Serializable, E extends BaseEntityInterface<I>> void defaultSimplifiedValuesEntityToDto(
            final BaseDto<I> dto, final E ent) {
        dto.setId(ent.getId());
    }
}
