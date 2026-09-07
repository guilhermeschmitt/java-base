package br.com.guilherme.javabase.dispatcher;

/**
 * Momentos do ciclo de vida de uma entidade em que um {@link BaseDispatcher} pode registrar
 * um callback via {@code addEvent}.
 */
public enum DBEventType {
    BEFORE_INSERT,
    AFTER_INSERT,
    BEFORE_UPDATE,
    AFTER_UPDATE,
    BEFORE_DELETE,
    AFTER_DELETE
}
