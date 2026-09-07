package br.com.guilherme.javabase.controller;

/**
 * Identifica cada operacao CRUD exposta por {@link BaseController}, para permitir que uma
 * implementacao bloqueie metodos especificos via {@code getBloquedMethodsCore()}.
 */
public enum ControllerMethodsCore {
    GET,
    GETALL,
    POST,
    UPDATE,
    DELETE
}
