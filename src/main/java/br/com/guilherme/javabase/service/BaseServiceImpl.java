package br.com.guilherme.javabase.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.util.Assert;

import com.querydsl.core.types.Predicate;

import br.com.guilherme.javabase.domain.BaseEntityInterface;
import br.com.guilherme.javabase.exception.BadRequestException;
import br.com.guilherme.javabase.exception.EntityNotFound;
import br.com.guilherme.javabase.exception.ErrorSeverity;
import br.com.guilherme.javabase.exception.EnumErrorMessage;
import br.com.guilherme.javabase.exception.ExceptionDatabaseImpl;
import br.com.guilherme.javabase.exception.UnauthorizedException;
import br.com.guilherme.javabase.repository.BaseRepository;
import br.com.guilherme.javabase.util.GenericClassUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Transactional(rollbackFor = Exception.class)
public abstract class BaseServiceImpl<T extends BaseEntityInterface<I>, I extends Serializable>
        implements BaseService<T, I> {

    protected final BaseRepository<T, I> repository;

    @PersistenceContext
    protected EntityManager em;

    @Autowired
    private ExceptionDatabaseImpl exceptionDatabase;

    protected boolean allowsDeletion = true;
    protected boolean allowsEditing = true;

    public BaseServiceImpl(final BaseRepository<T, I> repository) {
        this.repository = repository;
    }

    @Override
    public List<T> getByList(final List<I> ids) {
        if (Objects.isNull(ids) || ids.isEmpty())
            throw new BadRequestException(ErrorSeverity.ERROR, EnumErrorMessage.ERR_INVALID_ID);
        return this.repository.findAllById(ids);
    }

    @Override 
    public Optional<T> findById(final I id) {
        if(Objects.isNull(id))
            throw new BadRequestException(ErrorSeverity.ERROR, EnumErrorMessage.ERR_INVALID_ID);

        try {
            return repository.findById(id);
        } catch(final Exception e) {
            throw exceptionDatabase.translateMessageDatabase(e);
        }
    }

    @Override 
    public Optional<T> findOneByPredicate(final Predicate predicate) {
        return repository.findOne(predicate);
    }

    @Override 
    public T getOne(final I id) {
        return findById(id).orElseThrow(() -> notFoundById(id));
    }

    @Override
    public T getOneIfPresent(final I id) {
        if(id != null)
            return getOne(id);
        return null;
    }

    @Override 
    public T getOneByPredicate(final Predicate predicate) {
        final Optional<T> find = this.findOneByPredicate(predicate);
        if(find.isEmpty())
            throw notFoundById(null);
        return find.get();
    }

    @Override 
    public Optional<T> findByIdReference(final I id) {
        if(id == null)
            throw new BadRequestException(ErrorSeverity.ERROR, EnumErrorMessage.ERR_INVALID_ID);

        T r = null;

        if(repository.existsById(id)) {
            r = newInstanceEntity();
            r.setId(id);
        }

        return Optional.ofNullable(r);
    }

    @Override 
    public T getOneReference(final I id) {
        return findByIdReference(id).orElseThrow(() -> notFoundById(id));
    }

    @Override 
    public Page<T> findAllPredicate(final Pageable page, final Predicate predicate) {
        if(predicate != null)
            return repository.findAll(predicate, page);
        return Page.empty();
    }

    @Override 
    public T insert(final T t) {
        try {
            final T x = repository.save(beforeSave(t));
            saveChildRecords(x);
            return afterSave(x);
        } catch(final Exception e) {
            throw exceptionDatabase.translateMessageDatabase(e);
        }
    }

    @Override 
    public T insertAndFlush(final T t) {
        final T db = this.insert(t);
        em.flush();
        return db;
    }

    @Override 
    public T update(final T t) {
        if(!allowsEditing)
            throw new UnauthorizedException(ErrorSeverity.ERROR, EnumErrorMessage.ERR_EDIT_NOT_ALLOWED_IN_CONTEXT);

        try {
            final T ent = repository.save(beforeSave(t));
            saveChildRecords(ent);
            return afterSave(ent);
        } catch(final Exception e) {
            throw exceptionDatabase.translateMessageDatabase(e);
        }
    }

    @Override 
    public T updateAndFlush(final T t) {
        final T db = this.update(t);
        em.flush();
        return db;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override 
    public List<T> saveAndCommitParalelTransaction(final Iterable<T> entities) {
        Assert.notNull(entities, "Entities must not be null");

        final List<T> result = new ArrayList<>();

        for(final T entity : entities)
            result.add(insert(entity));

        return result;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void deleteAndCommitParalelTransaction(final Iterable<T> entities) {
        Assert.notNull(entities, "Entities must not be null");
        repository.deleteAllInBatch(entities);
    }

    @Override
    public boolean deleteById(final I id) {
        if(!allowsDeletion)
            throw new UnauthorizedException(ErrorSeverity.ERROR, EnumErrorMessage.ERR_DELETION_NOT_ALLOWED_IN_CONTEXT);

        final T t = getOne(id);
        beforeDelete(t);

        try {
            removeChildRecords(t);
            t.setActive(Boolean.FALSE);
            repository.save(t);
            em.flush();
            afterDelete(t);

            return true;
        } catch(final Exception e) {
            throw exceptionDatabase.translateMessageDatabase(e);
        }
    }

    @Override
    public Page<T> findAll(final Pageable page) {
        return repository.findAll(page);
    }

    @Override
    public List<T> findAll() {
        return repository.findAll();
    }

    @Override 
    public List<T> findByPredicate(final Predicate predicate) {
        if(predicate == null)
            return new ArrayList<>();
        return StreamSupport.stream(repository.findAll(predicate).spliterator(), false).toList();
    }

    @Override 
    public T beforeSave(final T ent) {
        return ent;
    }

    @Override 
    public boolean existsById(final I id) {
        return repository.existsById(id);
    }

    public EntityNotFound notFoundById(final I id) {
        return new EntityNotFound(ErrorSeverity.ERROR, EnumErrorMessage.ERR_RECORD_NOT_EXIST, generateDescription(), id);
    }

    public String generateDescription() {
        return null;
    }

    protected T newInstanceEntity() {
        return new GenericClassUtil<T>(getClass()).getNewInstance("T");
    }

    protected T afterSave(final T ent) {
        return ent;
    }

    protected void saveChildRecords(final T t) {

    }

    protected void removeChildRecords(final T t) {

    }

    protected void beforeDelete(final T t) {

    }

    protected void afterDelete(final T t) {

    }



}
