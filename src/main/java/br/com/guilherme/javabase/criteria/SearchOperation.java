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

    private String symbol;
    private boolean multiValue;

    SearchOperation(final String symbol) {
        multiValue = false;
    }

    SearchOperation(final String symbol, final boolean multiValue) {
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

    
}
