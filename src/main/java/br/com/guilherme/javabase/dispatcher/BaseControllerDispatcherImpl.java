package br.com.guilherme.javabase.dispatcher;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;

import com.querydsl.core.types.EntityPath;

import br.com.guilherme.javabase.converter.BaseConverterDto;
import br.com.guilherme.javabase.domain.BaseEntityInterface;
import br.com.guilherme.javabase.dto.BaseDto;
import br.com.guilherme.javabase.dto.BaseDtoBasic;
import br.com.guilherme.javabase.exception.EnumErrorMessage;
import br.com.guilherme.javabase.exception.ErrorSeverity;
import br.com.guilherme.javabase.exception.UpdateError;
import br.com.guilherme.javabase.service.BaseService;
import br.com.guilherme.javabase.util.ResponseUtil;
import br.com.guilherme.javabase.validator.DtoValidator;

public class BaseControllerDispatcherImpl<D extends BaseDto<I>, U extends BaseDto<I>, P extends BaseDtoBasic, E extends BaseEntityInterface<I>, I extends Serializable, B extends BaseDto<I>>
        extends BaseDispatcherImpl<D, U, P, E, I, B> implements BaseControllerDispatcher<D, U, P, E, I, B> {

    public BaseControllerDispatcherImpl(final BaseService<E, I> bsService,
            final BaseConverterDto<D, U, P, E, I, B> baseConverterDto, final EntityPath<E> entityPath) {
        this(bsService, baseConverterDto, entityPath, null);
    }

    public BaseControllerDispatcherImpl(final BaseService<E, I> bsService,
            final BaseConverterDto<D, U, P, E, I, B> baseConverterDto, final EntityPath<E> entityPath,
            final DtoValidator<U, P> validator) {
        super(bsService, baseConverterDto, entityPath, validator);
    }

    @Override
    public ResponseEntity<D> getObject(final I id) {
        final Optional<E> ent = Optional.ofNullable(service.getOne(id));

        return executeMethod(() -> {
            if (ent.isPresent() && getValidSelectedObject().test(ent.get()))
                return ResponseUtil.processResponse(converter.toDto(ent.get()));
            return ResponseUtil.noContent();
        });
    }

    @Override
    public ResponseEntity<Page<B>> getObjectList(final Pageable page, final String search) {
        final Page<E> pageItem = service.findAllPredicate(page, verifyCurrentFilter(search));

        return executeMethod(() -> {
            final Page<B> pageDto = converter.toPageDto(pageItem);
            return ResponseUtil.processResponse(pageDto);
        });
    }

    @Override
    public ResponseEntity<Page<B>> getObjectList(final Pageable page, final String search, final List<I> listID) {
        if (!CollectionUtils.isEmpty(listID))
            return ResponseUtil.processResponse(new PageImpl<>(converter.toListDto(service.getByList(listID))));
        return getObjectList(page, search);
    }

    @Override
    public ResponseEntity<D> postObject(final P dto, final Boolean response, final Boolean responseID) {
        return executeMethod(() -> {
            if (validator != null)
                validator.validateCreate(dto);

            final Function<E, Optional<D>> convertFunction = getConvertFunction(response, responseID);
            final Optional<D> optD = processPost(dto).map(convertFunction::apply).filter(Optional::isPresent).map(Optional::get);

            return ResponseUtil.processResponsePost(optD);
        });
    }

    @Override
    public ResponseEntity<D> updateObject(final I id, final U dto, final Boolean response, final Boolean responseID) {
        if (Objects.isNull(id))
            throw new UpdateError(ErrorSeverity.FATAL, EnumErrorMessage.ERR_ID_NOT_INFORMED);
        if (!id.equals(dto.getId()))
            throw new UpdateError(ErrorSeverity.FATAL, EnumErrorMessage.ERR_NOT_ALLOWED);

        final E ent = service.getOne(id);

        if (!getValidSelectedObject().test(ent))
            return ResponseUtil.noContent();

        if (validator != null)
            validator.validateUpdate(dto);

        final Function<E, Optional<D>> convertFunction = getConvertFunction(response, responseID);

        return executeMethod(() -> ResponseUtil.processResponsePut(converter.fromDtoU(dto, ent)
                .map(this::executeEventUpdate).map(convertFunction::apply).filter(Optional::isPresent).map(Optional::get)));
    }

    @Override
    public ResponseEntity<Object> deleteObject(final I id) {
        E ent = null;
        if (event.containsKey(DBEventType.BEFORE_DELETE) || event.containsKey(DBEventType.AFTER_DELETE))
            ent = service.getOne(id);

        if (event.containsKey(DBEventType.BEFORE_DELETE))
            event.get(DBEventType.BEFORE_DELETE).apply(ent);

        service.deleteById(id);

        if (event.containsKey(DBEventType.AFTER_DELETE))
            event.get(DBEventType.AFTER_DELETE).apply(ent);

        return ResponseUtil.ok();
    }

    protected Optional<E> processPost(final P dto) {
        final Optional<E> newEntOptional = converter.fromDtoP(dto, Optional.empty());
        E newEnt = null;

        if (newEntOptional.isPresent())
            newEnt = executeEventInsert(newEntOptional.get());
        return Optional.ofNullable(newEnt);
    }

}
