package br.com.guilherme.javabase.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

public interface BaseEntityInterface<I extends Serializable> extends Serializable {
    
    void setId(I id);

    I getId();

    LocalDateTime getCreatedAt();
    
    LocalDateTime getUpdatedAt();

    Long getCreatedBy();

    Long getUpdatedBy();

    void auditInsert();

    void auditUpdate();

}
