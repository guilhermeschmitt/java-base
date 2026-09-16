package br.com.guilherme.javabase.dto;

/**
 * Implementada por enums que precisam ser expostos ao front-end como opcoes {label, value} (ver
 * {@link EnumOptionDto} / {@code BaseEnumControllerImpl}) - {@code value} e' sempre o proprio
 * {@code name()} da constante, so' o rotulo legivel vem de {@link #getLabel()}.
 */
public interface LabeledEnum {

    String getLabel();
}
