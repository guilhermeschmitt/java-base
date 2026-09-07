package br.com.guilherme.javabase.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

/**
 * Instancia, via reflection, a classe concreta ligada a um dos parametros de tipo genericos
 * declarados pela superclasse de {@code concreteClass} (ex.: o {@code E} de
 * {@code MinhaEntidadeConverter extends BaseConverterDtoImpl<D, U, P, E, S, I, B>}).
 *
 * <p>O parametro de tipo e resolvido pelo nome declarado na superclasse (ex.: {@code "E"}),
 * nao pela posicao: reordenar os generics dessa superclasse nao quebra quem ja chama
 * {@link #getNewInstance(String)}.
 */
public class GenericClassUtil<T> {

    private final Class<?> concreteClass;

    public GenericClassUtil(final Class<?> concreteClass) {
        this.concreteClass = concreteClass;
    }

    public T getNewInstance(final String typeParameterName) {
        final Class<T> type = resolveTypeArgument(typeParameterName);
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (final ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "Nao foi possivel instanciar " + type.getName() + " (parametro de tipo \"" + typeParameterName
                            + "\" de " + concreteClass.getName() + "); certifique-se de que ha um construtor publico sem argumentos",
                    e);
        }
    }

    @SuppressWarnings("unchecked")
    private Class<T> resolveTypeArgument(final String typeParameterName) {
        Class<?> currentClass = concreteClass;
        Type genericSuperclass = currentClass.getGenericSuperclass();

        while (!(genericSuperclass instanceof ParameterizedType) && currentClass.getSuperclass() != null) {
            currentClass = currentClass.getSuperclass();
            genericSuperclass = currentClass.getGenericSuperclass();
        }

        if (!(genericSuperclass instanceof final ParameterizedType parameterizedType))
            throw new IllegalStateException(
                    "Nenhuma superclasse generica parametrizada foi encontrada para " + concreteClass.getName());

        final Class<?> declaringClass = (Class<?>) parameterizedType.getRawType();
        final TypeVariable<?>[] typeParameters = declaringClass.getTypeParameters();

        int typeParameterIndex = -1;
        for (int i = 0; i < typeParameters.length; i++) {
            if (typeParameters[i].getName().equals(typeParameterName)) {
                typeParameterIndex = i;
                break;
            }
        }

        if (typeParameterIndex < 0)
            throw new IllegalArgumentException("O parametro de tipo \"" + typeParameterName + "\" nao existe em "
                    + declaringClass.getName());

        final Type typeArgument = parameterizedType.getActualTypeArguments()[typeParameterIndex];
        if (!(typeArgument instanceof Class<?>))
            throw new IllegalStateException("O parametro de tipo \"" + typeParameterName + "\" de "
                    + concreteClass.getName() + " nao e uma classe concreta: " + typeArgument);

        return (Class<T>) typeArgument;
    }
}
