package net.aopacloud.superbi.queryEngine.sql.aggregator;

import com.google.common.base.Strings;
import net.aopacloud.superbi.common.core.utils.StringUtils;

public interface Aggregator {

    String getFunction();

    default String replaceIfFunction(String expression) {
        return expression;
    }

    default String buildAggregationExpression(String field){
        return StringUtils.format(getFunction(), field);
    }

    default String unfoldFunction(String func,String expression) {
        if (Strings.isNullOrEmpty(expression)) {
            return expression;
        }
        return expression.substring(func.length() +1 ,expression.length()-1);
    }
}
