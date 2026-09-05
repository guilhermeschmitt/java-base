package br.com.guilherme.javabase.validator;

import br.com.guilherme.javabase.dto.BaseDto;
import br.com.guilherme.javabase.dto.BaseDtoBasic;

public interface DtoValidator<U extends BaseDto<?>, P extends BaseDtoBasic> {
    
    void validateCreate(P dto);

    void validateUpdate(U dto);

}
