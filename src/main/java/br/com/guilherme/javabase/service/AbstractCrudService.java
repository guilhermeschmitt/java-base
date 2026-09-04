package br.com.guilherme.javabase.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Implementacao padrao de {@link CrudService}. Os services concretos so precisam informar
 * o repositorio a ser usado por meio de {@link #repository()}.
 */
public abstract class AbstractCrudService<T, ID> implements CrudService<T, ID> {

    protected abstract JpaRepository<T, ID> repository();

    @Override
    public T save(T entity) {
        return repository().save(entity);
    }

    @Override
    public Optional<T> findById(ID id) {
        return repository().findById(id);
    }

    @Override
    public List<T> findAll() {
        return repository().findAll();
    }

    @Override
    public void deleteById(ID id) {
        repository().deleteById(id);
    }
}
