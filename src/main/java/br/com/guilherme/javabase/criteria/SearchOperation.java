package br.com.guilherme.javabase.criteria;

public enum SearchOperation implements BaseEnum<String> {

    EQUAL("=="),
    NOT_EQUAL("!="),
    GREATER_THAN_OR_EQUAL(">="),
    GREATER_THAN(">"),
    LESS_THAN_OR_EQUAL("<="),
    LESS_THAN("<"),
    LIKE("=lk="),
    CONTAINS("=ctn="),
    IN("=in=", true),
    NOT_IN("=nin=", true),
    BETWEEN("=bt=", true),
    STARTS_WITH("=sw="),
    ENDS_WITH("=ew=");

    private final String symbol;
    private final boolean multiValue;

    SearchOperation(final String symbol) {
        this(symbol, false);
    }

    SearchOperation(final String symbol, final boolean multiValue) {
        this.symbol = symbol;
        this.multiValue = multiValue;
    }

    public String getSymbol() {
        return symbol;
    }

    public boolean isMultiValue(){
        return multiValue;
    }

    @Override
    public String getValue() {
        return getSymbol();
    }

    public static SearchOperation fromSymbol(final String symbol) {
        for (final SearchOperation operation : values())
            if (operation.symbol.equals(symbol))
                return operation;
        throw new IllegalArgumentException("Operador RSQL desconhecido: " + symbol);
    }
}
