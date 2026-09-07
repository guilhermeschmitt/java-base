package br.com.guilherme.javabase.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

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
        createdAt = LocalDateTime.now();

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
        createdAt = LocalDateTime.now();

        Long userId;

        try {
            userId = SecurityUtils.getLoggedUserId();
        } catch (Exception e) {
            userId = 0L;
        }

        updatedBy = userId;
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
