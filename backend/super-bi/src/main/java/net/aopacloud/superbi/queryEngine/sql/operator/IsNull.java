package net.aopacloud.superbi.queryEngine.sql.operator;

/**
 * @author: hudong
 * @date: 2023/8/16
 * @description:
 */
public class IsNull implements Operator {
    @Override
    public String apply(OperatorParam param) {
        if (param.getDateType().isText()) {
            return String.format("(%s is null or %s = '')", param.getExpression(), param.getExpression());
        } else {
            return String.format("%s is null", param.getExpression());
        }
    }
}
