package br.com.guilherme.javabase.exception;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

import org.springframework.context.i18n.LocaleContextHolder;

/**
 * Codigos de erro de negocio padronizados, usados pelas excecoes de {@link DomainException}
 * em toda a biblioteca. Cada constante aponta para uma chave no resource bundle
 * {@code error-messages}, resolvida de acordo com o {@link Locale} da requisicao atual
 * (via {@link LocaleContextHolder}).
 *
 * <p>O bundle padrao ({@code error-messages.properties}) esta em ingles. Traducoes sao
 * adicionadas criando um arquivo {@code error-messages_<locale>.properties} no mesmo pacote
 * (ja existe uma extensao para {@code pt_BR}); qualquer locale sem arquivo proprio cai de
 * volta no ingles.
 *
 * <p>A busca usa {@link Control#getNoFallbackControl} de proposito: o comportamento padrao do
 * {@link ResourceBundle} cai para o locale padrao da JVM (nao para o bundle raiz) quando o
 * locale pedido nao tem arquivo proprio, o que faria uma requisicao em qualquer idioma
 * devolver mensagens em portugues sempre que a aplicacao rodasse numa maquina/JVM configurada
 * com locale pt_BR. Desligando esse fallback implicito, o unico fallback e o bundle raiz
 * (ingles), como esperado.
 */
public enum EnumErrorMessage {

    ERR_INVALID_ID("err.invalid.id"),
    ERR_RECORD_NOT_EXIST("err.record.not.exist"),
    ERR_EDIT_NOT_ALLOWED_IN_CONTEXT("err.edit.not.allowed.in.context"),
    ERR_DELETION_NOT_ALLOWED_IN_CONTEXT("err.deletion.not.allowed.in.context"),
    ERR_RECORD_DOES_NOT_ALLOW("err.record.does.not.allow"),
    ERR_ID_NOT_INFORMED("err.id.not.informed"),
    ERR_NOT_ALLOWED("err.not.allowed"),
    ERR_FIELD_NOT_FOUND("err.field.not.found"),
    ERR_INVALID_FILTER("err.invalid.filter"),
    ERR_FILTER_PROCESSING_FAILED("err.filter.processing.failed"),
    ERR_PARAMETER_BLANK("err.parameter.blank");

    private static final String BUNDLE_BASE_NAME = "br.com.guilherme.javabase.exception.error-messages";
    private static final Control NO_JVM_DEFAULT_FALLBACK = Control.getNoFallbackControl(Control.FORMAT_PROPERTIES);

    private final String key;

    EnumErrorMessage(final String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public String getMessage() {
        return getMessage(LocaleContextHolder.getLocale());
    }

    public String getMessage(final Locale locale) {
        return ResourceBundle.getBundle(BUNDLE_BASE_NAME, locale, NO_JVM_DEFAULT_FALLBACK).getString(key);
    }
}
