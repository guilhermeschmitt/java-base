package br.com.guilherme.javabase.controller;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
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
import br.com.guilherme.javabase.security.AccessGuard;
import br.com.guilherme.javabase.service.BaseService;
import br.com.guilherme.javabase.util.HttpServletRequestUtils;
import br.com.guilherme.javabase.util.ResponseUtil;
import br.com.guilherme.javabase.validator.DtoValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

public abstract class BaseControllerImpl<D extends BaseDto<I>, U extends BaseDto<I>, P extends BaseDtoBasic, E extends BaseEntityInterface<I>, I extends Serializable, B extends BaseDto<I>>
        implements BaseController<D, U, P, I, B> {

    private @Autowired HttpServletRequest httpServletRequest;
    private @Autowired AccessGuard accessGuard;
    private Set<ControllerMethodsCore> bloquedMethodsCore;
    private Map<ControllerMethodsCore, Set<String>> allowedRolesCore;
    private Map<String, Map<String, String>> uniqueRules;

    private final BaseControllerDispatcher<D, U, P, E, I, B> dispatcher;

    public BaseControllerImpl(final BaseService<E, I> bsService,
            final BaseConverterDto<D, U, P, E, I, B> dBaseConverterDtoImpl, final EntityPath<E> entityPath) {
        this(bsService, dBaseConverterDtoImpl, entityPath, null);
    }

    public BaseControllerImpl(final BaseService<E, I> bsService,
            final BaseConverterDto<D, U, P, E, I, B> dBaseConverterDtoImpl, final EntityPath<E> entityPath,
            final DtoValidator<U, P> validator) {
        dispatcher = new BaseControllerDispatcherImpl<>(bsService, dBaseConverterDtoImpl, entityPath, validator);
    }

    public BaseControllerImpl(final BaseControllerDispatcherImpl<D, U, P, E, I, B> dispatcher) {
        this.dispatcher = dispatcher;
    }

    /**
     * {@code GET <recurso>/unique?field=<campo>&value=<valor>[&id=<id>][&<escopo>=<valor>...]}: responde
     * {@code { "unique": true|false }} - se nenhum outro registro (inativos inclusos, mesmo filtro de tenant
     * das outras consultas) tem esse valor no campo, dentro do escopo. {@code id} exclui o proprio registro
     * (edicao). So' os campos declarados em {@link #getUniqueRules()} podem ser consultados (lista branca);
     * sem nenhum declarado o endpoint responde 501, como um metodo bloqueado. Mesmo controle de acesso
     * do GET ALL. E' uma checagem de conveniencia pro formulario: o save continua validando por conta propria.
     */
    @GetMapping(path = "/unique")
    public ResponseEntity<Map<String, Boolean>> checkUnique(@RequestParam(value = "field") final String field,
            @RequestParam(value = "value") final String value,
            @RequestParam(value = "id", required = false) final I id,
            @RequestParam final Map<String, String> params) {
        if (getUniqueRules().isEmpty())
            return ResponseUtil.notImplemented();
        checkAccess(ControllerMethodsCore.GETALL);
        return getDispatcher().checkUnique(getUniqueRules(), field, value, id, params);
    }

    /**
     * Lista branca do {@code GET /unique}: campo da entidade -> escopo ({@code parametro da query ->
     * caminho na entidade}). O controller concreto popula no construtor, no mesmo estilo de
     * {@link #getAllowedRolesCore()}, ex.:
     * {@code getUniqueRules().put("documentNumber", Map.of("typeService", "typeService.id"))} = documento
     * unico por tipo de servico ({@code ?field=documentNumber&value=X&typeService=3}). Escopo vazio
     * ({@code Map.of()}) = unico no client inteiro. Todo parametro do escopo e' obrigatorio na consulta.
     */
    public Map<String, Map<String, String>> getUniqueRules() {
        if (uniqueRules == null)
            uniqueRules = new HashMap<>();
        return uniqueRules;
    }

    @Override
    @GetMapping(path = "/{id}")
    public ResponseEntity<D> getObject(@PathVariable(name = "id", required = true) final I id) {
        if (getBloquedMethodsCore().contains(ControllerMethodsCore.GET))
            return ResponseUtil.notImplemented();
        checkAccess(ControllerMethodsCore.GET);
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
        checkAccess(ControllerMethodsCore.GETALL);
        return getDispatcher().getObjectList(page, search, listaID);
    }

    @Override
    @PostMapping
    public ResponseEntity<D> postObject(@RequestBody(required = true) @Valid final P dto,
            @RequestParam(value = "response", required = false, defaultValue = "false") final Boolean response,
            @RequestParam(value = "responseID", required = false, defaultValue = "false") final Boolean responseID) {
        if (getBloquedMethodsCore().contains(ControllerMethodsCore.POST))
            return ResponseUtil.notImplemented();
        checkAccess(ControllerMethodsCore.POST);
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
        checkAccess(ControllerMethodsCore.UPDATE);
        return getDispatcher().updateObject(id, dto, response, responseID);
    }

    @Override
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Object> deleteObject(@PathVariable(name = "id", required = true) final I id) {
        if (getBloquedMethodsCore().contains(ControllerMethodsCore.DELETE))
            return ResponseUtil.notImplemented();
        checkAccess(ControllerMethodsCore.DELETE);
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

    /**
     * Roles permitidas por operacao CRUD, no mesmo estilo de {@link #getBloquedMethodsCore()}:
     * o controller concreto popula esse map no proprio construtor, ex.
     * {@code getAllowedRolesCore().put(ControllerMethodsCore.DELETE, Set.of("ADMIN"))}.
     * Uma operacao sem entrada aqui fica liberada pra qualquer usuario autenticado (mesmo
     * comportamento de antes desta checagem existir) - so' restringe o que for explicitamente
     * declarado.
     */
    public Map<ControllerMethodsCore, Set<String>> getAllowedRolesCore() {
        if (allowedRolesCore == null)
            allowedRolesCore = new HashMap<>();
        return allowedRolesCore;
    }

    /**
     * Identificador do recurso exposto por este controller, usado por implementacoes de
     * {@link AccessGuard} mais granulares que role simples (ex.: permissao por pagina/acao).
     * {@code null} por padrao - a implementacao padrao ({@code RoleAccessGuard}) ignora esse
     * valor.
     */
    protected String getResourceCode() {
        return null;
    }

    private void checkAccess(final ControllerMethodsCore method) {
        accessGuard.checkAccess(getResourceCode(), method,
                getAllowedRolesCore().getOrDefault(method, Set.of()));
    }
}
