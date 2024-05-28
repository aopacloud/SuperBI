package net.aopacloud.superbi.queryEngine.sql.aggregator;

import net.aopacloud.superbi.common.core.utils.StringUtils;

public interface Aggregator {

    String getFunction();

    default String buildAggregationExpression(String field){
        return StringUtils.format(getFunction(), field);
    }
}
