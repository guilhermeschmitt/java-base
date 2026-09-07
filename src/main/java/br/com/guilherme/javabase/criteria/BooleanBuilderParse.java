package br.com.guilherme.javabase.criteria;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.DatePath;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.ListPath;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.SetPath;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.core.types.dsl.TimePath;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.RSQLParserException;
import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import cz.jirutka.rsql.parser.ast.LogicalNode;
import cz.jirutka.rsql.parser.ast.LogicalOperator;
import cz.jirutka.rsql.parser.ast.Node;

import br.com.guilherme.javabase.exception.BadRequestException;
import br.com.guilherme.javabase.exception.EnumErrorMessage;
import br.com.guilherme.javabase.exception.ErrorSeverity;

public class BooleanBuilderParse {

    private static RSQLParser parser;

    static {
        final Set<ComparisonOperator> operators = new HashSet<>();

        for (final SearchOperation op : SearchOperation.values())
            operators.add(new ComparisonOperator(op.getSymbol(), op.isMultiValue()));

        parser = new RSQLParser(operators);
    }

    BooleanBuilderParse() {
    }

    public static <E> Predicate parse(final EntityPath<E> entityPath, final String search) {
        try {
            final Node node = parseToNode(search);
            return processNode(entityPath, node);
        } catch (final RSQLParserException e) {
            throw new BadRequestException(ErrorSeverity.ERROR, EnumErrorMessage.ERR_INVALID_FILTER, e.getMessage());
        }
    }

    public static Node parseToNode(final String search) {
        return parser.parse(formatQuery(search));
    }

    private static String formatQuery(String query) {
        if (query == null)
            return null;
        if (query.isEmpty())
            return query;

        StringBuilder delimiters = new StringBuilder("");
        for (final SearchOperation op : SearchOperation.values())
            delimiters.append("(?=").append(op.getSymbol()).append(")|");

        delimiters.deleteCharAt(delimiters.length() - 1);

        String[] nodes = query.split(delimiters.toString());

        StringBuilder sb = new StringBuilder();

        for (String node : nodes)
            sb.append(formatClause(node));

        return sb.toString();
    }

    private static String formatClause(String clause) {
        if(clause == null)
            return null;

        StringBuilder sb = new StringBuilder(clause);
        int first = clause.indexOf('\"');
        int last = clause.lastIndexOf('\"');

        if(clause.endsWith("\\\"\""))
            last++;
        if(first != last) {
            for(int i = first + 1; i < last; i++) {
                if(sb.charAt(i) == '\"' || sb.charAt(i) == '\\') {
                    sb.insert(i, '\\');
                    i++;
                }
            }
        }

        return sb.toString();
    }

    private static <E> Predicate processNode(final EntityPath<E> entityPath, final Node node) {
        if (node instanceof LogicalNode) {
            final LogicalNode logical = (LogicalNode) node;

            final BooleanBuilder out = new BooleanBuilder();
            if (LogicalOperator.OR.equals(logical.getOperator())) {
                for (final Node children : logical.getChildren())
                    out.or(processNode(entityPath, children));
            } else if (LogicalOperator.AND.equals(logical.getOperator())) {
                for (final Node children : logical.getChildren())
                    out.and(processNode(entityPath, children));
            }

            return out;
        } else {
            final ComparisonNode comparisonNode = (ComparisonNode) node;
            final SpecSearchCriteria spec = new SpecSearchCriteria(comparisonNode.getSelector(),
                    comparisonNode.getOperator().getSymbols(), comparisonNode.getArguments());
            return proccessItem(spec, entityPath);
        }
    }

    public static String[] splitNivel(final String campo) {
        return campo.split("\\.", 2);
    }

    private static <E> BooleanExpression proccessItem(final SpecSearchCriteria item, final EntityPath<E> entityPath) {
        try {
            final String[] items = splitNivel(item.getKey());
            final Field xEntityPathBase = entityPath.getClass().getField(items[0]);
            final Object xField = xEntityPathBase.get(entityPath);

            SpecSearchCriteria newItem = null;

            if (items.length > 1) {
                newItem = new SpecSearchCriteria();
                newItem.setKey(items[1]);
                newItem.setValue(item.getValue());
                newItem.setOperation(item.getOperation());
            }

            if (xField instanceof EntityPathBase && newItem != null)
                return getGenericPredicate(newItem, (EntityPathBase<?>) xField);
            else if (xField instanceof ListPath && newItem != null)
                return listPath(newItem, (ListPath<?, ?>) xField);
            else if (xField instanceof SetPath && newItem != null)
                return setPath(newItem, (SetPath<?, ?>) xField);
            else if (xField instanceof SimpleExpression && newItem != null)
                return processFilter((SimpleExpression<?>) xField, item.getOperation(), item.getValue());
            return null;
        } catch (final NoSuchFieldException e) {
            throw new BadRequestException(ErrorSeverity.FATAL, EnumErrorMessage.ERR_FIELD_NOT_FOUND, e.getMessage());
        } catch (final IllegalAccessException e) {
            throw new BadRequestException(ErrorSeverity.FATAL, EnumErrorMessage.ERR_FILTER_PROCESSING_FAILED,
                    String.format("Illegal access: %s", e.getMessage()));
        } catch (final IllegalArgumentException e) {
            throw new BadRequestException(ErrorSeverity.FATAL, EnumErrorMessage.ERR_FILTER_PROCESSING_FAILED,
                    String.format("Illegal argument: %s", e.getMessage()));
        } catch (final SecurityException e) {
            throw new BadRequestException(ErrorSeverity.FATAL, EnumErrorMessage.ERR_FILTER_PROCESSING_FAILED,
                    String.format("Security exception: %s", e.getMessage()));
        }
    }

    public static BooleanExpression getGenericPredicate(final SpecSearchCriteria item, final EntityPath<?> entityPath) {
        try {
            final String[] items = splitNivel(item.getKey());
            final Field xEntityPathBase = entityPath.getClass().getField(items[0]);
            final Object xField = xEntityPathBase.get(entityPath);

            SpecSearchCriteria newItem = null;

            if(items.length > 1) {
                 newItem = new SpecSearchCriteria();
                newItem.setKey(items[1]);
                newItem.setValue(item.getValue());
                newItem.setOperation(item.getOperation());
            }

            if (xField instanceof EntityPathBase && newItem != null)
                return getGenericPredicate(newItem, (EntityPathBase<?>) xField);
            else if (xField instanceof ListPath && newItem != null)
                return listPath(newItem, (ListPath<?, ?>) xField);
            else if (xField instanceof SimpleExpression && newItem != null)
                return processFilter((SimpleExpression<?>) xField, item.getOperation(), item.getValue());
            return null;
        } catch (final NoSuchFieldException e) {
            throw new BadRequestException(ErrorSeverity.FATAL, EnumErrorMessage.ERR_FIELD_NOT_FOUND, e.getMessage());
        } catch (final IllegalAccessException e) {
            throw new BadRequestException(ErrorSeverity.FATAL, EnumErrorMessage.ERR_FILTER_PROCESSING_FAILED,
                    String.format("Illegal access: %s", e.getMessage()));
        } catch (final IllegalArgumentException e) {
            throw new BadRequestException(ErrorSeverity.FATAL, EnumErrorMessage.ERR_FILTER_PROCESSING_FAILED,
                    String.format("Illegal argument: %s", e.getMessage()));
        } catch (final SecurityException e) {
            throw new BadRequestException(ErrorSeverity.FATAL, EnumErrorMessage.ERR_FILTER_PROCESSING_FAILED,
                    String.format("Security exception: %s", e.getMessage()));
        }
    }

    private static BooleanExpression listPath(final SpecSearchCriteria item, final ListPath<?, ?> xField) throws NoSuchFieldException, IllegalAccessException {
        final String[] lowLevel = splitNivel(item.getKey());

        final SimpleExpression<?> origin = xField.any();
        final Field nField = origin.getClass().getField(lowLevel[0]);
        final Object vField = nField.get(origin);

        final SpecSearchCriteria newItem = new SpecSearchCriteria();

        newItem.setKey(lowLevel[0]);

        if(lowLevel.length > 1) 
            newItem.setKey(lowLevel[1]);

        newItem.setValue(item.getValue());
        newItem.setOperation(item.getOperation());

        if(vField instanceof EntityPathBase)
            return getGenericPredicate(newItem, (EntityPathBase<?>) vField);
        else if(vField instanceof ListPath)
            return listPath(newItem, (ListPath<?,?>) xField);
        else if(vField instanceof SimpleExpression)
            return processFilter((SimpleExpression<?>) vField, item.getOperation(), item.getValue());
        return null;
    }

    private static BooleanExpression setPath(final SpecSearchCriteria item, final SetPath<?, ?> xField) throws NoSuchFieldException, IllegalAccessException {
        final String[] lowLevel = splitNivel(item.getKey());

        final SimpleExpression<?> origin = xField.any();
        final Field nField = origin.getClass().getField(lowLevel[0]);
        final Object vField = nField.get(origin);

        final SpecSearchCriteria newItem = new SpecSearchCriteria();

        newItem.setKey(lowLevel[0]);

        if(lowLevel.length > 1) 
            newItem.setKey(lowLevel[1]);

        newItem.setValue(item.getValue());
        newItem.setOperation(item.getOperation());

        if(vField instanceof EntityPathBase)
            return getGenericPredicate(newItem, (EntityPathBase<?>) vField);
        else if(vField instanceof SetPath)
            return setPath(newItem, (SetPath<?,?>) xField);
        else if(vField instanceof SimpleExpression)
            return processFilter((SimpleExpression<?>) vField, item.getOperation(), item.getValue());
        return null;
    }

    private static BooleanExpression processFilter(final SimpleExpression<?>  expression, final SearchOperation type, final List<String> list) {
        if(expression instanceof StringPath)
            return ProcessFilter.string((StringPath) expression, type, list);
        else if(expression instanceof NumberExpression)
            return ProcessFilter.number((NumberExpression<?>) expression, type, list);
        else if(expression instanceof BooleanPath)
            return ProcessFilter.bool((BooleanPath) expression, type, list);
        else if(expression instanceof EnumPath)
            return ProcessFilter.enums((EnumPath<?>) expression, type, list);
        else if(expression instanceof DatePath)
            return ProcessFilter.date((DatePath<? super Comparable<?>>) expression, type, list);
        else if(expression instanceof TimePath)
            return ProcessFilter.time((TimePath<? super Comparable<?>>) expression, type, list);
        else if(expression instanceof DateTimeExpression)
            return ProcessFilter.dateTime((DateTimeExpression<? super Comparable<?>>) expression, type, list);
        return null;
    }
}
