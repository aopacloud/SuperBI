package net.aopacloud.superbi.queryEngine.model;

import lombok.Data;
import net.aopacloud.superbi.constant.BiConsist;

import java.util.Objects;

/**
 * @author: hudong
 * @date: 2023/8/14
 * @description:
 */
@Data
public class Measure {
    private String name;

    private String expression;

    private String displayName;

    private String dataType;

    private String aggregator;

    private String fastCompute;

    public String getId() {
        if (Objects.nonNull(fastCompute)) {
            return String.format("`%s%s%s%s%s`", name, BiConsist.FIELD_AGGREGATOR_SEPARATOR, aggregator, BiConsist.FIELD_AGGREGATOR_SEPARATOR, fastCompute);
        }
        return String.format("`%s%s%s`", name, BiConsist.FIELD_AGGREGATOR_SEPARATOR, aggregator);
    }

}