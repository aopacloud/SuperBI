package net.aopacloud.superbi.queryEngine.model;

import lombok.Data;

import java.util.List;

/**
 * @author: hudong
 * @date: 2023/8/14
 * @description:
 */
@Data
public class Sort {

    // 计算类型  all | group
    private String computeType;

    // 分组
    private List<String> sortGroup;

    private String sortType;

    private String sortField;

    private Integer limit;

    private String aggregator;
}
