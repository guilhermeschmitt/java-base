package br.com.guilherme.javabase.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Base para entidades vinculadas a um cliente especifico (suporte multi-client). O java-base
 * nao conhece a entidade Client de cada projeto consumidor, entao aqui {@code clientId} e
 * apenas a chave estrangeira crua (Long) - nao um relacionamento JPA navegavel. Um projeto que
 * queira {@code @ManyToOne} navegavel ate a sua propria entidade Client pode declara-lo por
 * conta propria em vez de estender esta classe.
 */
@Data
@MappedSuperclass
@EqualsAndHashCode(callSuper = true)
public class ClientBaseId extends SimpleBaseId {

    @Column(name = "client_id", nullable = false)
    private Long clientId;
}
