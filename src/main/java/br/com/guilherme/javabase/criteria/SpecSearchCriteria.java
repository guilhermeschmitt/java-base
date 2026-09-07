package br.com.guilherme.javabase.criteria;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SpecSearchCriteria {

    private String key;
    private SearchOperation operation;
    private List<String> value;

    public SpecSearchCriteria() {
    }

    public SpecSearchCriteria(final String key, final String[] operation, final List<String> value) {
        this.key = key;
        this.value = value;
        this.operation = SearchOperation.fromSymbol(operation[0]);
    }

    public SpecSearchCriteria(final String key, final SearchOperation operation, final @NotBlank String value) {
        this.key = key;
        this.operation = operation;
        this.value = new ArrayList<>(Arrays.asList(value));
    }

    public SpecSearchCriteria(final String key, final SearchOperation operation, final @NotNull Long value) {
        this(key, operation, value.toString());
    }

}
