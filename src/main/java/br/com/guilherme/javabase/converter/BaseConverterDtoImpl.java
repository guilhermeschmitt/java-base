package br.com.guilherme.javabase.converter;

import java.io.Serializable;
import java.lang.foreign.Linker.Option;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import br.com.guilherme.javabase.domain.BaseEntityInterface;
import br.com.guilherme.javabase.dto.BaseDto;
import br.com.guilherme.javabase.dto.BaseDtoBasic;
import br.com.guilherme.javabase.service.BaseService;

public abstract class BaseConverterDtoImpl<D extends BaseDto<I>, U extends BaseDto<I>, P extends BaseDtoBasic, E extends BaseEntityInterface<I>, S extends BaseService<E, I>, I extends Serializable, B extends BaseDto<I>>
        implements BaseConverterDto<D, U, P, E, I, B> {

    @Autowired
    protected S service;

    private ThreadLocal<Map<String, Object>> threadParams;

    @Override
    public final List<B> toListDto(final List<E> ent) {
        final List<B> ret = new ArrayList<>();
        for (final E item : ent)
            toBasicDto(item).ifPresent(ret::add);
        return ret;
    }

    @Override
    public Optional<B> toBasicDto(final E ent) {
        final B dto = newInstanceBasicData();
        setDtoDefaultValues(dto, ent);
        return mapToBasicDto(ent, dto);
    }

    @Override
    public final Page<B> toPageDto(final Page<E> listE) {
        return listE.map(e -> toBasicDto(e).orElse(null));
    }

    @Override
    public final Optional<D> toDto(final E ent) {
        if (ent != null) {
            final D dto = getNewInstanceD();

            setDtoDefaultValues(dto, ent);

            return mapEntityToDto(ent, dto);
        }

        return Optional.empty();
    }

    @Override
    public final Optional<D> toDtoSimplified(final E ent) {
        if (ent != null) {
            final D dto = getNewInstanceD();
            ConverterUtils.defaultSimplifiedValuesEntityToDto(dto, ent);
            return Optional.of(dto);
        }

        return Optional.empty();
    }

    @Override 
    public Optional<E> fromDtoP(final P dto, final Optional<I> id) {
        if(id.isPresent() && service.existsById(id.get()))
            throw new BadRequestException(ErrorSeverity.ERROR, EnumErrorMessage.ERR_RECORD_DOES_NOT_ALLOW, " a duplicate ID", id);
        else
            return mapDtoToEntityForInsert(dto, getNewInstanceE());
    }

    @Override 
    public Optional<E> fromDtoU(final U dto, final E ent) {
        return mapDtoToEntityForUpdate(dto, ent);
    }

    @Override
    public final void setParamsController(final ThreadLocal<Map<String, Object>> params) {
        this.threadParams = params;
    }
 
    protected B newInstanceBasicData() {
        return new GenericClassUtil<B>(getClass()).getNewInstance(6);
    }

    protected E getNewInstanceE() {
        return new GenericClassUtil<E>(getClass()).getNewInstance(3);
    }

    protected D getNewInstanceD() {
        return new GenericClassUtil<D>(getClass()).getNewInstance(0);
    }
    
    protected void setDtoDefaultValues(final BaseDto<I> dto, final E ent) {
        ConverterUtils.defaultValuesEntityToDto(dto, ent);
    }

    @SuppressWarnings("unchecked")
    protected final <T> T getParamController(final String key) {
        if(!Objects.isNull(this.threadParams.get()) && this.threadParams.get().containsKey(key))
            return (T) this.threadParams.get().get(key);
        throw new BadRequestException().of(ErrorSeverity.ERROR, EnumErrorMessage.ERR_RECORD_DOES_NOT_ALLOW, String.format(" %s parameter is blank ", key));
    }

    protected boolean isInvalid(Object... values) {
        for(Object value : values) {
            if(Objects.isNull(value))
                return true;
        }

        return false;
    }

    protected abstract Optional<B> mapToBasicDto(E ent, B dto);

    protected abstract Optional<E> mapDtoToEntityForInsert(P dto, E ent);

    protected abstract Optional<E> mapDtoToEntityForUpdate(U dto, E ent);

    protected abstract Optional<D> mapEntityToDto(E ent, D dto);

}
