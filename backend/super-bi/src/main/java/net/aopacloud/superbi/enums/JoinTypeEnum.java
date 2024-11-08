package net.aopacloud.superbi.enums;

public enum JoinTypeEnum {
    LEFT(" left join "),
    RIGHT(" right join "),
    FULL(" full join "),
    INNER(" inner join ");

    private String expression;
    JoinTypeEnum(String expression) {
        this.expression = expression;
    }

    public String getExpression() {
        return expression;
    }
}
