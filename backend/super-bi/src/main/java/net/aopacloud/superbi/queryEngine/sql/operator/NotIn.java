package net.aopacloud.superbi.queryEngine.sql.operator;

import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Strings;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: hudong
 * @date: 2023/8/16
 * @description:
 */
public class NotIn implements Operator{
    @Override
    public String apply(OperatorParam param) {
        List<String> args = param.getArgs().stream()
                .filter(arg -> !Strings.isNullOrEmpty(arg))
                .map(arg -> param.getDateType().isText() ? String.format("'%s'", arg) : arg)
                .collect(Collectors.toList());
        if(args.isEmpty()) {
            return StringUtils.EMPTY;
        }
        return String.format("%s not in (%s)",param.getExpression(), Joiner.on(",").join(args));
    }
}
