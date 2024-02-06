package net.aopacloud.superbi.queryEngine.model;

import lombok.Data;
import net.aopacloud.superbi.queryEngine.enums.AggregatorEnum;

/**
 * @author: hudong
 * @date: 2023/8/14
 * @description:
 */
@Data
public class Sort {

    private String sortType;

    private String sortField;

    private Integer limit;

    private AggregatorEnum aggregator;
}
