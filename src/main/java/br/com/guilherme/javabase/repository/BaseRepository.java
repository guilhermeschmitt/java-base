package br.com.guilherme.javabase.repository;

import java.io.Serializable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Repositorio base opcional. Os repositorios concretos de cada projeto podem estender esta
 * interface em vez de {@link JpaRepository} diretamente, para permitir a adicao futura de
 * metodos comuns sem quebrar os projetos que ja a utilizam.
 */
@NoRepositoryBean
public interface BaseRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {
}
