package br.com.guilherme.javabase.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import br.com.guilherme.javabase.util.SecurityUtils;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
@MappedSuperclass
public abstract class SimpleAuditory<I extends Serializable> implements BaseEntityInterface<I> {

    @Setter
    @Column(name = "created_by")
    private Long createdBy;

    @Setter
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Setter(AccessLevel.PROTECTED)
    @Column(name = "updated_by")
    private Long updatedBy;

    @Setter(AccessLevel.PROTECTED)
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void setImportedCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public void auditInsert() {
        createdAt = now();

        Long userId;

        try {
            userId = SecurityUtils.getLoggedUserId();
        } catch (Exception e) {
            userId = 0L;
        }

        createdBy = userId;
        updatedBy = userId;
        updatedAt = createdAt;
    }

    @Override
    public void auditUpdate() {
        updatedAt = now();

        Long userId;

        try {
            userId = SecurityUtils.getLoggedUserId();
        } catch (Exception e) {
            userId = 0L;
        }

        updatedBy = userId;
    }

    // Truncado pra microssegundos porque toda coluna `updated_at`/`created_at`
    // dos serviços consumidores é um `TIMESTAMP` (Postgres) sem precisão
    // explícita - o Postgres arredonda pra 6 dígitos ao persistir, então um
    // `LocalDateTime.now()` com nanossegundos (9 dígitos) fica com um valor em
    // memória diferente do que de fato foi salvo. Isso causava 409 "fantasma"
    // no optimistic lock manual (ver `BaseControllerDispatcherImpl.updateObject`):
    // o cache de GET guardava o valor em memória (pré-arredondamento), a
    // checagem de stale-update relia o valor já arredondado do banco, e a
    // comparação `isBefore` divergia sempre que o arredondamento do Postgres
    // empurrava o timestamp persistido pra frente do valor cacheado.
    private static LocalDateTime now() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
    }

    @PrePersist
    public void validateCreateDate() {
        if (createdAt == null)
            auditInsert();
    }

    @PreUpdate
    public void validateAuditUpdate() {
        if (createdAt == null)
            auditInsert();
        else
            auditUpdate();
    }

}
