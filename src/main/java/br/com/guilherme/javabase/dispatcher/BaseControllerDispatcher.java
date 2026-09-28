package br.com.guilherme.javabase.dispatcher;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import br.com.guilherme.javabase.domain.BaseEntityInterface;
import br.com.guilherme.javabase.dto.BaseDto;
import br.com.guilherme.javabase.dto.BaseDtoBasic;

public interface BaseControllerDispatcher<D extends BaseDto<I>, U extends BaseDto<I>, P extends BaseDtoBasic, E extends BaseEntityInterface<I>, I extends Serializable, B extends BaseDto<I>>
        extends BaseDispatcher<E> {

    ResponseEntity<D> getObject(I id);

    ResponseEntity<Page<B>> getObjectList(Pageable page, String filtro);

    ResponseEntity<Page<B>> getObjectList(Pageable page, String filtro, List<I> listaID);

    ResponseEntity<D> postObject(P dto, Boolean response, Boolean responseID);

    ResponseEntity<D> updateObject(I id, U dto, Boolean response, Boolean responseID);

    ResponseEntity<Object> deleteObject(I id);

    ResponseEntity<Map<String, Boolean>> checkUnique(Map<String, Map<String, String>> rules, String field,
            String value, I id, Map<String, String> params);

}
