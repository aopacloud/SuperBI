package net.aopacloud.superbi.queryEngine.sql.operator;

/**
 * @author: hudong
 * @date: 2023/8/16
 * @description:
 */
public class Equal implements Operator {

    @Override
    public String apply(OperatorParam param) {

        String arg = param.getArgs().get(0);

        if (param.getDateType().isText() || param.getDateType().isTime()) {
            return String.format(" %s='%s'", param.getExpression(), arg);
        }

        return String.format(" %s=%s", param.getExpression(), arg);
    }
}
