package br.com.guilherme.javabase.dispatcher;

import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;

public interface BaseDispatcher<E> {
    
    void setValidSelectedObject(Predicate<E> validSelectedObject);

    void setPredicateDefault(final BooleanBuilder predicateDefault);

    Supplier<BooleanExpression> getCurrentFilter();

    void addEvent(final DBEventType type, final UnaryOperator<E> dispatcher);

    void setCurrentFilter(Supplier<BooleanExpression> currentFilter);

    Predicate<E> getValidSelectedObject();

    void setParamsController(Supplier<Map<String, Object>> params);

}
