package br.com.guilherme.javabase.criteria;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.DatePath;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.core.types.dsl.TemporalExpression;
import com.querydsl.core.types.dsl.TimePath;

public class ProcessFilter {
    ProcessFilter() {
    }

    public static BooleanExpression date(final DatePath<? super Comparable<?>> expression, final SearchOperation type,
            final List<String> list) {
        LocalDate value = null;

        if (!isNull(list.get(0)))
            value = LocalDate.parse(list.get(0));

        switch (type) {
            case GREATER_THAN_OR_EQUAL:
                return expression.goe(value);
            case GREATER_THAN:
                return expression.gt(value);
            case LESS_THAN_OR_EQUAL:
                return expression.loe(value);
            case LESS_THAN:
                return expression.lt(value);
            case NOT_EQUAL:
                if (value == null)
                    return expression.isNotNull();
                else
                    return expression.ne(value);
            case EQUAL:
                if (value == null)
                    return expression.isNull();
                else
                    return expression.eq(value);
            case BETWEEN:
                final LocalDate value2 = LocalDate.parse(list.get(1));
                return expression.between(value, value2);
            case IN:
                return expression.in(getDates(list));
            case NOT_IN:
                return expression.notIn(getDates(list));
            default:
                return null;
        }
    }

    public static BooleanExpression time(final TimePath<? super Comparable<?>> expression, final SearchOperation type,
            final List<String> list) {
        LocalTime value = null;

        if (!isNull(list.get(0)))
            value = LocalTime.parse(list.get(0));

        switch (type) {
            case GREATER_THAN_OR_EQUAL:
                return expression.goe(value);
            case GREATER_THAN:
                return expression.gt(value);
            case LESS_THAN_OR_EQUAL:
                return expression.loe(value);
            case LESS_THAN:
                return expression.lt(value);
            case NOT_EQUAL:
                if (value == null)
                    return expression.isNotNull();
                else
                    return expression.ne(value);
            case EQUAL:
                if (value == null)
                    return expression.isNull();
                else
                    return expression.eq(value);
            case BETWEEN:
                final LocalTime value2 = LocalTime.parse(list.get(1));
                return expression.between(value, value2);
            case IN:
                return expression.in(getTimes(list));
            case NOT_IN:
                return expression.notIn(getTimes(list));
            default:
                return null;
        }
    }

    public static BooleanExpression string(final StringPath expression, final SearchOperation type,
            final List<String> values) {
        final String value = values.get(0);

        switch (type) {
            case CONTAINS:
                return expression.containsIgnoreCase(value);
            case EQUAL:
                return expression.equalsIgnoreCase(value);
            case NOT_EQUAL:
                return expression.notEqualsIgnoreCase(value);
            case IN:
                return expression.in(values);
            case NOT_IN:
                return expression.notIn(values);
            case LIKE:
                return expression.likeIgnoreCase(value);
            case STARTS_WITH:
                return expression.startsWithIgnoreCase(value);
            case ENDS_WITH:
                return expression.endsWithIgnoreCase(value);
            case GREATER_THAN_OR_EQUAL:
                return expression.goe(value);
            case GREATER_THAN:
                return expression.gt(value);
            case LESS_THAN_OR_EQUAL:
                return expression.loe(value);
            case LESS_THAN:
                return expression.lt(value);
            default:
                return null;
        }
    }

    @SuppressWarnings("unchecked")
    protected static BooleanExpression number(final NumberExpression expression, final SearchOperation type,
            final List<String> list) {
        Number value = null;

        if (!isNull(list.get(0)))
            value = getValue(expression, list, 0);

        switch (type) {
            case GREATER_THAN_OR_EQUAL:
                return expression.goe(value);
            case GREATER_THAN:
                return expression.gt(value);
            case LESS_THAN_OR_EQUAL:
                return expression.loe(value);
            case LESS_THAN:
                return expression.lt(value);
            case NOT_EQUAL:
                if (value == null)
                    return expression.isNotNull();
                else
                    return expression.ne(value);
            case EQUAL:
                if (value == null)
                    return expression.isNull();
                else
                    return expression.eq(value);
            case BETWEEN:
                Number value2 = null;
                if (list.size() == 2)
                    value2 = getValue(expression, list, 1);
                return expression.between(value, value2);
            case IN:
                return expression.in(getValues(expression, list));
            case NOT_IN:
                return expression.notIn(getValues(expression, list));
            default:
                return null;
        }
    }

    protected static BooleanExpression bool(final BooleanPath expression, final SearchOperation type,
            final List<String> list) {

        final String value = list.get(0);

        switch (type) {
            case EQUAL:
                if (isNull(value))
                    return expression.isNull();
                else
                    return expression.eq(Boolean.parseBoolean(value));
            case NOT_EQUAL:
                if (isNull(value))
                    return expression.isNotNull();
                else
                    return expression.ne(Boolean.parseBoolean(value));
            default:
                return null;
        }
    }

    public static BooleanExpression enums(final EnumPath<?> expression, final SearchOperation type,
            final List<String> values) {
        switch (type) {
            case EQUAL:
            case IN:
                if (values == null)
                    return expression.isNull();
                else
                    return expression.stringValue().in(values);
            case NOT_EQUAL:
            case NOT_IN:
                if (values == null)
                    return expression.isNotNull();
                else
                    return expression.stringValue().notIn(values);
            default:
                return null;
        }
    }

    public static BooleanExpression dateTime(TemporalExpression<? super Comparable<?>> expression,
            final SearchOperation type,
            final List<String> values) {
        final String firstItem = values.get(0);
        if (isNull(firstItem))
            return null;

        boolean onlyDate = false;

        if (!firstItem.contains("T"))
            onlyDate = true;

        final LocalDateTime value1 = convertDate(firstItem, onlyDate);

        switch (type) {
            case GREATER_THAN_OR_EQUAL:
                return expression.goe(value1);
            case GREATER_THAN:
                return expression.gt(value1);
            case LESS_THAN_OR_EQUAL:
                return expression.loe(value1);
            case LESS_THAN:
                return expression.lt(value1);
            case NOT_EQUAL:
                if (value1 == null)
                    return expression.isNotNull();
                else
                    return expression.ne(value1);
            case EQUAL:
                if (value1 == null)
                    return expression.isNull();
                else
                    return expression.eq(value1);
            case BETWEEN:
                String secondItem = values.size() == 1 ? null : values.get(1);
                final LocalDateTime value2 = convertDate(secondItem, !secondItem.contains("T"));
                return expression.between(value1, value2);
            case IN:
                return expression.in(getDateTime(values, onlyDate));
            case NOT_IN:
                return expression.notIn(getDateTime(values, onlyDate));
            default:
                return null;
        }
    }

    public static boolean isNull(final String value) {
        return "null".equalsIgnoreCase(value);
    }

    public static List<LocalDate> getDates(final List<String> list) {
        final List<LocalDate> returnList = new ArrayList<>();
        for (final String val : list)
            returnList.add(LocalDate.parse(val));
        return returnList;
    }

    public static List<LocalTime> getTimes(final List<String> list) {
        final List<LocalTime> returnList = new ArrayList<>();
        for (final String val : list)
            returnList.add(LocalTime.parse(val));
        return returnList;
    }

    public static List<LocalDateTime> getDateTime(final List<String> values, final Boolean onlyDate) {
        final List<LocalDateTime> returnList = new ArrayList<>();
        for (final String val : values)
            returnList.add(convertDate(val, onlyDate));
        return returnList;
    }

    public static LocalDateTime convertDate(final String item, final boolean onlyDate) {
        if (onlyDate)
            return LocalDate.parse(item).atStartOfDay();
        return LocalDateTime.parse(item);
    }

    private static Number getValue(final NumberExpression<?> expression, final List<String> values, final int i) {
        Number value = null;

        if (expression.getType().getTypeName().equals(BigDecimal.class.getName()))
            value = new BigDecimal(values.get(1));
        else if (expression.getType().getTypeName().equals(Long.class.getName()))
            value = Long.parseLong(values.get(1));
        else if (expression.getType().getTypeName().equals(Integer.class.getName()))
            value = Integer.parseInt(values.get(1));

        return value;
    }

    private static List<Number> getValues(final NumberExpression<?> expression, final List<String> values) {
        List<Number> returnValues = new ArrayList<>();

        for (int j = 0; j < values.size(); j++)
            returnValues.add(getValue(expression, values, j));

        return returnValues;
    }
}
