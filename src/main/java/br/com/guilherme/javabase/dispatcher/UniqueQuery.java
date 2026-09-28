package br.com.guilherme.javabase.dispatcher;

import java.lang.reflect.Field;
import java.util.Map;

import org.springframework.core.convert.support.DefaultConversionService;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;

import br.com.guilherme.javabase.exception.BadRequestException;
import br.com.guilherme.javabase.exception.EnumErrorMessage;
import br.com.guilherme.javabase.exception.ErrorSeverity;

/**
 * Monta o predicado do {@code GET <recurso>/unique} (ver {@link BaseControllerDispatcherImpl#checkUnique}):
 * {@code campo = valor} e {@code caminho = parametro} pra cada item do escopo, excluindo o proprio registro
 * ({@code id}) quando informado. Caminhos podem navegar relacionamentos ({@code "typeService.id"}); os
 * valores chegam como texto da query string e sao convertidos pro tipo do atributo da entidade.
 */
final class UniqueQuery {

    private UniqueQuery() {
    }

    static Predicate build(final EntityPath<?> root, final String fieldPath, final String value,
            final Map<String, String> scope, final Map<String, String> params, final Object id) {
        final BooleanBuilder predicate = new BooleanBuilder(eq(root, fieldPath, value));
        scope.forEach((param, path) -> {
            final String scopeValue = params.get(param);
            if (scopeValue == null || scopeValue.isBlank())
                throw new BadRequestException(ErrorSeverity.ERROR, EnumErrorMessage.ERR_PARAMETER_BLANK, param);
            predicate.and(eq(root, path, scopeValue));
        });
        if (id != null)
            predicate.andNot(eq(root, "id", String.valueOf(id)));
        return predicate;
    }

    private static Predicate eq(final EntityPath<?> root, final String dottedPath, final String rawValue) {
        Path<?> path = root;
        Class<?> type = root.getType();
        final String[] segments = dottedPath.split("\\.");
        for (final String segment : segments) {
            type = fieldType(type, segment);
            path = Expressions.path(type, path, segment);
        }
        return ExpressionUtils.eqConst(castPath(path), convert(rawValue, type, dottedPath));
    }

    private static Class<?> fieldType(final Class<?> owner, final String name) {
        for (Class<?> current = owner; current != null; current = current.getSuperclass()) {
            try {
                final Field field = current.getDeclaredField(name);
                return field.getType();
            } catch (final NoSuchFieldException ex) {
                // sobe pra superclasse (ex.: id/clientId nas classes base)
            }
        }
        throw new IllegalStateException("Unique rule points to a missing attribute: " + owner.getSimpleName() + "." + name);
    }

    private static Object convert(final String rawValue, final Class<?> type, final String path) {
        try {
            return DefaultConversionService.getSharedInstance().convert(rawValue, type);
        } catch (final RuntimeException ex) {
            throw new BadRequestException(ErrorSeverity.ERROR, EnumErrorMessage.ERR_INVALID_VALUE, path);
        }
    }

    @SuppressWarnings("unchecked")
    private static Path<Object> castPath(final Path<?> path) {
        return (Path<Object>) path;
    }
}
