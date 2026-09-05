package br.com.guilherme.javabase.converter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;

import br.com.guilherme.javabase.domain.BaseEntityInterface;
import br.com.guilherme.javabase.dto.BaseDto;
import br.com.guilherme.javabase.dto.BaseDtoBasic;

public interface BaseConverterDto<D extends BaseDto<I>, U extends BaseDto<I>, P extends BaseDtoBasic, E extends BaseEntityInterface<I>, I extends Serializable, B extends BaseDto<I>> {

    Optional<E> fromDtoP(P dto, Optional<I> id);

    Optional<E> fromDtoU(U dto, E e);

    Optional<D> toDto(E ent);

    List<B> toListDto(List<E> ent);

    Page<B> toPageDto(Page<E> listE);

    Optional<B> toBasicDto(final E ent);

    void setParamsController(ThreadLocal<Map<String, Object>> params);

    Optional<D> toDtoSimplified(E ent);

}
