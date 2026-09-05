package br.com.guilherme.javabase.controller;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.tomcat.util.http.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.EntityPath;

import br.com.guilherme.javabase.converter.BaseConverterDto;
import br.com.guilherme.javabase.dispatcher.BaseControllerDispatcher;
import br.com.guilherme.javabase.dispatcher.BaseControllerDispatcherImpl;
import br.com.guilherme.javabase.domain.BaseEntityInterface;
import br.com.guilherme.javabase.dto.BaseDto;
import br.com.guilherme.javabase.dto.BaseDtoBasic;
import br.com.guilherme.javabase.service.BaseService;
import br.com.guilherme.javabase.validator.DtoValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Transactional(rollbackFor = Exception.class)
public abstract class BaseControllerImpl<D extends BaseDto<I>, U extends BaseDto<I>, P extends BaseDtoBasic, E extends BaseEntityInterface<I>, I extends Serializable, B extends BaseDto<I>>
        implements BaseController<D, U, P, I, B> {

    private @Autowired HttpServletRequest httpServletRequest;
    private Set<ControllerMethodsCore> bloquedMethodsCore;

    private final BaseControllerDispatcher<D, U, P, E, I, B> dispatcher;

    public BaseControllerImpl(final BaseService<E, I> bsService,
            final BaseConverterDto<D, U, P, E, I, B> dBaseConverterDtoImpl, final EntityPath<E> entityPath) {
        this(bsService, dBaseConverterDtoImpl, entityPath, null);
    }

    public BaseControllerImpl(final BaseService<E, I> bsService,
            final BaseConverterDto<D, U, P, E, I, B> dBaseConverterDtoImpl, final EntityPath<E> entityPath,
            final DtoValidator<U, P> validator) {
        dispatcher = new BaseControllerDispatcherImpl(bsService, dBaseConverterDtoImpl, entityPath, validator);
    }

    public BaseControllerImpl(final BaseControllerDispatcherImpl<D, U, P, E, I, B> dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    @GetMapping(path = "/{id}")
    public ResponseEntity<D> getObject(@PathVariable(name = "id", required = true) final I id) {
        if (getBloquedMethodsCore().contains(ControllerMethodsCore.GET))
            return ResponseUtil.notImplemented();
        return getDispatcher().getObject(id);
    }

    @Override
    @GetMapping
    public ResponseEntity<Page<B>> getObjectList(
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) final Pageable page,
            @RequestParam(value = "search", required = false) final String search,
            @RequestParam(value = "listaID", required = false) final List<I> listaID) {
        if (getBloquedMethodsCore().contains(ControllerMethodsCore.GETALL))
            return ResponseUtil.notImplemented();
        return getDispatcher().getObjectList(page, search, listaID);
    }

    @Override
    @PostMapping
    public ResponseEntity<D> postObject(@RequestBody(required = true) @Valid final P dto,
            @RequestParam(value = "response", required = false, defaultValue = "false") final Boolean response,
            @RequestParam(value = "responseID", required = false, defaultValue = "false") final Boolean responseID) {
        if (getBloquedMethodsCore().contains(ControllerMethodsCore.POST))
            return ResponseUtil.notImplemented();
        return getDispatcher().postObject(dto, response, responseID);
    }

    @Override
    @PutMapping(path = "/{id}")
    public ResponseEntity<D> updateObject(@PathVariable(name = "id", required = true) final I id,
            @RequestBody(required = true) @Valid final U dto,
            @RequestParam(value = "response", required = false, defaultValue = "false") final Boolean response,
            @RequestParam(value = "responseID", required = false, defaultValue = "false") final Boolean responseID) {
        if (getBloquedMethodsCore().contains(ControllerMethodsCore.UPDATE))
            return ResponseUtil.notImplemented();
        return getDispatcher().updateObject(id, dto, response, responseID);
    }

    @Override
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Object> deleteObject(@PathVariable(name = "id", required = true) final I id) {
        if (getBloquedMethodsCore().contains(ControllerMethodsCore.DELETE))
            return ResponseUtil.notImplemented();
        return getDispatcher().deleteObject(id);
    }

    protected static BooleanBuilder getPredicateDefaultFilterForGetAll() {
        return null;
    }

    public BaseControllerDispatcher<D, U, P, E, I, B> getDispatcher() {
        return dispatcher;
    }

    protected Optional<String> getPathValue(final String name) {
        return HttpServletRequestUtils.getPathValue(httpServletRequest, name);
    }

    protected String[] getParamValues(final String name) {
        if (httpServletRequest.getParameterMap().containsKey(name))
            return httpServletRequest.getParameterMap().get(name);
        return new String[] {};
    }

    protected String getParamValue(final String name) {
        final String[] values = getParamValues(name);
        if (values != null && values.length > 0)
            return values[0];
        return null;
    }

    protected Long getUrlParameter(final String parameterName) {
        return Long.valueOf(getPathValue(parameterName).orElseThrow());
    }

    public Set<ControllerMethodsCore> getBloquedMethodsCore() {
        if (bloquedMethodsCore == null)
            bloquedMethodsCore = new HashSet<>();
        return bloquedMethodsCore;
    }
}
