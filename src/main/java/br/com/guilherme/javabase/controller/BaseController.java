package br.com.guilherme.javabase.controller;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import br.com.guilherme.javabase.dto.BaseDto;
import br.com.guilherme.javabase.dto.BaseDtoBasic;

public interface BaseController<D extends BaseDto<I>, U extends BaseDto<I>, P extends BaseDtoBasic, I extends Serializable, B extends BaseDto<I>> {

    ResponseEntity<Page<B>> getObjectList(Pageable page, String search, List<I> listaID);

    ResponseEntity<D> getObject(I id);

    ResponseEntity<D> postObject(P dto, Boolean response, Boolean responseId);

    ResponseEntity<D> updateObject(I id, U dto, Boolean response, Boolean responseId);

    ResponseEntity<Object> deleteObject(I id);

}