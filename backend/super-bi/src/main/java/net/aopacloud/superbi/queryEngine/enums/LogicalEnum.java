package net.aopacloud.superbi.queryEngine.enums;

import java.util.List;

public enum LogicalEnum {

    AND(" and "),
    OR(" or ");

    private String expression;

    LogicalEnum(String expression) {
        this.expression = expression;
    }

    public String getExpression() {
        return this.expression;
    }

    public boolean match(List<Boolean> condition) {
        return this == AND ? condition.stream().allMatch(Boolean::booleanValue) : condition.stream().anyMatch(Boolean::booleanValue);
    }
}
