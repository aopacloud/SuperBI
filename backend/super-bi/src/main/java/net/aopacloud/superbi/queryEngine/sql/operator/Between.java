package net.aopacloud.superbi.queryEngine.sql.operator;

import net.aopacloud.superbi.common.core.utils.StringUtils;

/**
 * @author: hudong
 * @date: 2023/8/21
 * @description:
 */
public class Between implements Operator{
    @Override
    public String apply(OperatorParam param) {

        if(param.getArgs().size() != 2) {
            return StringUtils.EMPTY;
        }

        String start = param.getArgs().get(0);
        String end = param.getArgs().get(1);

        if(param.getDateType().isText() || param.getDateType().isTime()) {
            return String.format("%s between '%s' and '%s'",param.getExpression(), start, end);
        }

        return String.format("%s between %s and %s", param.getExpression() ,start, end);
    }
}
