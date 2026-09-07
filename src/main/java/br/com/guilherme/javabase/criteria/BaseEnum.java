package br.com.guilherme.javabase.criteria;

/**
 * Enum cujas constantes carregam um valor externo (ex.: o simbolo RSQL de {@link SearchOperation}).
 */
public interface BaseEnum<T> {

    T getValue();
}
