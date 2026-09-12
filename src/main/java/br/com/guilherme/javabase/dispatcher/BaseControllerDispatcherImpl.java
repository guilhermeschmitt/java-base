package br.com.guilherme.javabase.dispatcher;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;

import com.querydsl.core.types.EntityPath;

import br.com.guilherme.javabase.converter.BaseConverterDto;
import br.com.guilherme.javabase.domain.BaseEntityInterface;
import br.com.guilherme.javabase.domain.ClientBaseId;
import br.com.guilherme.javabase.dto.BaseDto;
import br.com.guilherme.javabase.dto.BaseDtoBasic;
import br.com.guilherme.javabase.exception.EnumErrorMessage;
import br.com.guilherme.javabase.exception.ErrorSeverity;
import br.com.guilherme.javabase.exception.UpdateError;
import br.com.guilherme.javabase.service.BaseService;
import br.com.guilherme.javabase.util.CacheHolder;
import br.com.guilherme.javabase.util.ResponseUtil;
import br.com.guilherme.javabase.util.SecurityUtils;
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
        final Cache cache = getCache();
        final String cacheKey = cacheable(cache) ? cacheKey(id, SecurityUtils.getLoggedClientId()) : null;

        if (cacheKey != null) {
            final Cache.ValueWrapper wrapper = cache.get(cacheKey);
            if (wrapper != null) {
                @SuppressWarnings("unchecked")
                final D cached = (D) wrapper.get();
                return ResponseUtil.processResponse(Optional.ofNullable(cached));
            }
        }

        final Optional<E> ent = Optional.ofNullable(service.getOne(id));

        return executeMethod(() -> {
            if (ent.isPresent() && getValidSelectedObject().test(ent.get())) {
                final Optional<D> dto = converter.toDto(ent.get());
                if (cacheKey != null)
                    dto.ifPresent(d -> cache.put(cacheKey, d));
                return ResponseUtil.processResponse(dto);
            }
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
            final Optional<E> insertedEnt = processPost(dto);

            insertedEnt.ifPresent(this::cachePut);

            final Optional<D> optD = insertedEnt.map(convertFunction::apply).filter(Optional::isPresent).map(Optional::get);

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

        if (dto.getUpdatedAt() != null && ent.getUpdatedAt() != null && dto.getUpdatedAt().isBefore(ent.getUpdatedAt()))
            throw new UpdateError(ErrorSeverity.ERROR, EnumErrorMessage.ERR_STALE_UPDATE);

        if (validator != null)
            validator.validateUpdate(dto);

        final Function<E, Optional<D>> convertFunction = getConvertFunction(response, responseID);

        return executeMethod(() -> {
            final Optional<E> updatedEnt = converter.fromDtoU(dto, ent).map(this::executeEventUpdate);
            updatedEnt.ifPresent(this::cachePut);
            return ResponseUtil.processResponsePut(updatedEnt.map(convertFunction::apply).filter(Optional::isPresent).map(Optional::get));
        });
    }

    @Override
    public ResponseEntity<Object> deleteObject(final I id) {
        E ent = null;
        if (event.containsKey(DBEventType.BEFORE_DELETE) || event.containsKey(DBEventType.AFTER_DELETE))
            ent = service.getOne(id);

        if (event.containsKey(DBEventType.BEFORE_DELETE))
            event.get(DBEventType.BEFORE_DELETE).apply(ent);

        service.deleteById(id);
        cacheEvict(id);

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

    /**
     * Cache de GET por id ({@code getObject}), atualizado em toda escrita (create/update) e
     * invalidado em toda remocao - nao cobre {@code getObjectList} (paginacao/filtro geram
     * espaco de chaves grande demais pra valer a pena). Nome do cache Redis = nome simples da
     * entidade (ex. {@code User}); chave = {@code clientId:id} pra entidades {@link ClientBaseId}
     * (garante que um cache hit nunca vaze dado de um cliente pra outro - o filtro Hibernate de
     * multi-tenant so age em cima de query, nao de cache), ou so {@code id} pras entidades de
     * catalogo sem cliente. Sem {@code CacheManager} configurado, ou sem cliente resolvido pra
     * uma entidade {@link ClientBaseId}, o cache e simplesmente ignorado (sempre vai ao banco).
     *
     * <p>Um cache hit em {@code getObject} devolve a resposta direto, sem passar por
     * {@code getValidSelectedObject()} - nenhum controller usa esse hook hoje, mas se algum vier
     * a usar pra restringir visibilidade alem do isolamento por cliente, precisa ser levado em
     * conta aqui tambem.
     */
    private Cache getCache() {
        final CacheManager cacheManager = CacheHolder.getCacheManager();
        return cacheManager != null ? cacheManager.getCache(getEntityPath().getType().getSimpleName()) : null;
    }

    private boolean isClientScoped() {
        return ClientBaseId.class.isAssignableFrom(getEntityPath().getType());
    }

    private boolean cacheable(final Cache cache) {
        return cache != null && (!isClientScoped() || SecurityUtils.getLoggedClientId() != null);
    }

    private String cacheKey(final I id, final Long clientId) {
        return isClientScoped() ? clientId + ":" + id : String.valueOf(id);
    }

    private void cachePut(final E ent) {
        final Cache cache = getCache();
        if (!cacheable(cache))
            return;

        final Long clientId = ent instanceof final ClientBaseId clientEnt ? clientEnt.getClientId() : null;
        if (isClientScoped() && clientId == null)
            return;

        converter.toDto(ent).ifPresent(dto -> cache.put(cacheKey(ent.getId(), clientId), dto));
    }

    private void cacheEvict(final I id) {
        final Cache cache = getCache();
        if (!cacheable(cache))
            return;

        cache.evict(cacheKey(id, SecurityUtils.getLoggedClientId()));
    }

}
