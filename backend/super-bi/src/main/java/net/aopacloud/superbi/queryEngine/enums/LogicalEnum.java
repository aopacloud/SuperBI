package net.aopacloud.superbi.queryEngine.enums;

public enum LogicalEnum {

    AND(" and "),
    OR(" or ");

    private String expression;

    LogicalEnum(String expression) {
        this.expression = expression;
    }

    public String getExpression(){
        return this.expression;
    }
}
