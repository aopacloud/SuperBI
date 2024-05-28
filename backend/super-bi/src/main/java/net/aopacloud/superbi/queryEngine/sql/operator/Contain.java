package net.aopacloud.superbi.queryEngine.sql.operator;

import net.aopacloud.superbi.common.core.utils.StringUtils;

import java.util.Objects;

/**
 * @author: hudong
 * @date: 2023/8/16
 * @description:
 */
public class Contain implements Operator {

    @Override
    public String apply(OperatorParam param) {

        if (!param.getDateType().isText()) {
            return StringUtils.EMPTY;
        }

        String keyword = param.getArgs().get(0);
        if(Objects.nonNull(keyword)) {
            keyword = keyword.replaceAll("_","\\\\_").replaceAll("%", "\\\\%");
        }

        return StringUtils.format("{} like '%{}%'", param.getExpression(), keyword);
    }
}
