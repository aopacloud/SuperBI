package net.aopacloud.superbi.queryEngine.sql.operator;

/**
 * @author: hudong
 * @date: 2023/8/16
 * @description:
 */
public class IsNotNull implements Operator{
    @Override
    public String apply(OperatorParam param) {

        if(param.getDateType().isText()){
            return String.format("%s is not null and %s != ''", param.getExpression(), param.getExpression());
        } else {
            return String.format("%s is not null", param.getExpression());
        }

    }
}
