package br.com.guilherme.javabase.exception;

import org.springframework.http.HttpStatus;

/**
 * Excecao pronta para o caso mais comum de todos os projetos: recurso nao encontrado.
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    @Override
    public int httpStatus() {
        return HttpStatus.NOT_FOUND.value();
    }
}
