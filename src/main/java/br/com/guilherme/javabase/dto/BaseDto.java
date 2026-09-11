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

    /**
     * Ecoada pelo cliente numa atualizacao para indicar qual versao do registro ele leu por
     * ultimo. {@code BaseControllerDispatcherImpl.updateObject} compara este valor com o
     * {@code updatedAt} atual do banco e recusa a atualizacao (409) se o valor enviado for
     * anterior - indício de que o cliente esta editando uma versao desatualizada do registro.
     * Se nao informado, nenhuma checagem e feita.
     */
    private LocalDateTime updatedAt;
}
