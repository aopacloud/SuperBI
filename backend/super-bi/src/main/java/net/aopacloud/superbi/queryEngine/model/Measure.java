package net.aopacloud.superbi.queryEngine.model;

import net.aopacloud.superbi.constant.BiConsist;
import net.aopacloud.superbi.queryEngine.enums.AggregatorEnum;
import lombok.Data;

/**
 * @author: hudong
 * @date: 2023/8/14
 * @description:
 */
@Data
public class Measure{
    private String name;

    private String expression;

    private String displayName;

    private String dataType;

    private AggregatorEnum aggregator;

    public String getId(){
        return String.format("`%s%s%s`", name, BiConsist.FIELD_AGGREGATOR_SEPARATOR ,aggregator);
    }

}