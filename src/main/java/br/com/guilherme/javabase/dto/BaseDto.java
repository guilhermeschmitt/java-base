package br.com.guilherme.javabase.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

@Data 
public abstract class BaseDto<I extends Serializable> implements BaseDtoBasic {
    public abstract void setId(I id);

    public abstract I getId();

    private LocalDateTime createdAt;

    private Boolean active;
}
