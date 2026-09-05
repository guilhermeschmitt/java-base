package br.com.guilherme.javabase.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean 
public interface BaseRepository<T,I> extends JpaRepository<T,I>, QuerydslPredicateExecutor<T> {
    
    default Optional<T> getOptionalById(I id) {
        return findById(id);
    }

}
