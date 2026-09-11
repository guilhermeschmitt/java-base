package br.com.guilherme.javabase.domain;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

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
 *
 * <p>Toda entidade que estende esta classe herda o filtro Hibernate {@value #CLIENT_FILTER}, que
 * restringe automaticamente qualquer SELECT (inclusive busca por id, via
 * {@code applyToLoadByKey}) a {@code client_id = :clientId}. {@code BaseServiceImpl} habilita
 * esse filtro no inicio de cada operacao de leitura, usando
 * {@link br.com.guilherme.javabase.util.SecurityUtils#getLoggedClientId()} como valor do
 * parametro - sem cliente resolvido para a requisicao, o filtro fica desligado e nada e
 * restrito. O filtro so cobre leituras; insert/update sao protegidos separadamente em
 * {@code BaseServiceImpl.verifyClientOwnership}.
 */
@Data
@MappedSuperclass
@EqualsAndHashCode(callSuper = true)
@FilterDef(name = ClientBaseId.CLIENT_FILTER, parameters = @ParamDef(name = "clientId", type = Long.class), applyToLoadByKey = true)
@Filter(name = ClientBaseId.CLIENT_FILTER, condition = "client_id = :clientId")
public class ClientBaseId extends SimpleBaseId {

    public static final String CLIENT_FILTER = "clientFilter";

    @Column(name = "client_id", nullable = false)
    private Long clientId;
}
