package br.com.guilherme.javabase.dispatcher;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.util.StringUtils;

import br.com.guilherme.javabase.converter.BaseConverterDto;
import br.com.guilherme.javabase.criteria.BooleanBuilderParse;
import br.com.guilherme.javabase.domain.BaseEntityInterface;
import br.com.guilherme.javabase.dto.BaseDto;
import br.com.guilherme.javabase.dto.BaseDtoBasic;
import br.com.guilherme.javabase.service.BaseService;
import br.com.guilherme.javabase.validator.DtoValidator;

public abstract class BaseDispatcherImpl<D extends BaseDto<I>, U extends BaseDto<I>, P extends BaseDtoBasic, E extends BaseEntityInterface<I>, I extends Serializable, B extends BaseDto<I>>
        implements BaseDispatcher<E> {

    protected final BaseService<E, I> service;
    protected final BaseConverterDto<D, U, P, E, I, B> converter;
    protected final DtoValidator<U, P> validator;

    protected final Map<DBEventType, UnaryOperator<E>> event;

    private final EntityPath<E> entityPath;
    private final ThreadLocal<Map<String, Object>> mapParamsThreadLocal = new ThreadLocal<>();

    protected BooleanBuilder predicateDefault;

    private Predicate<E> validSelectedObject = x -> true;
    private Supplier<BooleanExpression> currentFilter = () -> null;
    private Supplier<Map<String, Object>> paramsController = () -> null;

    public BaseDispatcherImpl(final BaseService<E, I> service, final BaseConverterDto<D, U, P, E, I, B> converter,
            final EntityPath<E> entityPath) {
        this(service, converter, entityPath, null);
    }

    public BaseDispatcherImpl(final BaseService<E, I> service, final BaseConverterDto<D, U, P, E, I, B> converter,
            final EntityPath<E> entityPath, final DtoValidator<U, P> validator) {
        this.service = service;
        this.validator = validator;
        this.converter = converter;
        this.entityPath = entityPath;

        converter.setParamsController(mapParamsThreadLocal);

        this.event = new EnumMap<>(DBEventType.class);
    }

    protected Function<E, Optional<D>> getConvertFunction(final Boolean response, final Boolean responseID) {
        if (Boolean.TRUE.equals(response))
            return converter::toDto;
        else if (Boolean.TRUE.equals(responseID))
            return converter::toDtoSimplified;
        return i -> Optional.empty();
    }

    protected <X> X executeMethod(final Supplier<X> process) {
        mapParamsThreadLocal.set(paramsController.get());
        try {
            return process.get();
        } finally {
            mapParamsThreadLocal.remove();
        }
    }

    @Override
    public void setParamsController(final Supplier<Map<String, Object>> params) {
        paramsController = params;
    }

    protected BooleanBuilder verifyCurrentFilter(final String search) {
        final BooleanBuilder predicate = new BooleanBuilder(currentFilter.get());
        predicate.and(predicateDefault);
        if(!StringUtils.isNullOrEmpty(search))
            predicate.and(BooleanBuilderParse.parse(entityPath, search));
        return predicate;
    }

    @Override 
    public void addEvent(final DBEventType type, final UnaryOperator<E> dispatcher) {
        event.put(type, dispatcher);
    }

    @Override 
    public void setPredicateDefault(final BooleanBuilder predicateDefault) {
        this.predicateDefault = predicateDefault;
    }

    @Override 
    public Supplier<BooleanExpression> getCurrentFilter() {
        return currentFilter;
    }

    @Override 
    public void setCurrentFilter(final Supplier<BooleanExpression> currentFilter) {
        this.currentFilter = currentFilter;
    }

    @Override 
    public Predicate<E> getValidSelectedObject() {
        return validSelectedObject;
    }

    @Override 
    public void setValidSelectedObject(final Predicate<E> validSelectedObject) {
        this.validSelectedObject = validSelectedObject;
    }

    protected E executeEventInsert(final E ent) {
        E newEnt = ent;

        if(event.containsKey(DBEventType.BEFORE_INSERT))
            newEnt = event.get(DBEventType.BEFORE_INSERT).apply(newEnt);

        newEnt = service.insert(newEnt);

        if(event.containsKey(DBEventType.AFTER_INSERT))
            newEnt = event.get(DBEventType.AFTER_INSERT).apply(newEnt);

        return newEnt;
    }

    protected E executeEventUpdate(final E ent) {
        E newEnt = ent;

        if(event.containsKey(DBEventType.BEFORE_UPDATE))
            newEnt = event.get(DBEventType.BEFORE_UPDATE).apply(newEnt);

        newEnt = service.update(newEnt);

        if(event.containsKey(DBEventType.AFTER_UPDATE))
            newEnt = event.get(DBEventType.AFTER_UPDATE).apply(newEnt);

        return newEnt;
    }
}
