package br.com.guilherme.javabase.service;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BaseService<T,I> {
    
    Optional<T> findById(I id);

    T getOne(I id);

    T insert(T t);

    T insertAndFlush(T t);
    
    T update(T t);
    
    T updateAndFlush(T t);

    boolean deleteById(I id);

    Page<T> findAll(Pageable page);

    List<T> findAll();

    Page<T> findAllPredicate(Pageable page, Predicate predicate);

    List<T> findByPredicate(final Predicate predicate);

    T getOneIfPresent(I id);

    List<T> getByList(List<I> ids);

    Optional<T> findOneByPredicate(final Predicate predicate);

    T beforeSave(T ent);

    List<T> saveAndCommitParalelTransaction(Iterable<T> entities);

    void deleteAndCommitParalelTransaction(Iterable<T> entities);

    boolean existsById(I id);

    T getOneByPredicate(Predicate predicate);

    T getOneReference(I id);

    Optional<T> findByIdReference(I id);
}
