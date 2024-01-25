package net.aopacloud.superbi.queryEngine.sql.operator;

/**
 * Functional operator enum.
 */
public enum FunctionalOperatorEnum {
    EMPTY(0, (param) -> "" ),
    EQUAL(1, new Equal()),
    NOT_EQUAL(2, new NotEqual()),
    START_WITH(3, new StartWith()),
    END_WITH(4, new EndWith()),
    CONTAIN(5, new Contain()),
    NOT_CONTAIN(6, new NotContain()),
    GT(7, new GT()),
    LT(8, new LT()),
    GTE(9, new GTE()),
    LTE(10, new LTE()),
    IN(11, new IN()),
    NOT_IN(12, new NotIn()),
    IS_NOT_NULL(13, new IsNotNull()),
    IS_NULL(14, new IsNull()),
    BETWEEN(15, new Between());

    private int code;
    private Operator operator;

    FunctionalOperatorEnum(int code, Operator operator) {
        this.code = code;
        this.operator = operator;
    }

    public Operator getOperator(){
        return operator;
    }

}
