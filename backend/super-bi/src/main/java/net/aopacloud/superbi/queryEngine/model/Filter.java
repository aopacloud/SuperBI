package net.aopacloud.superbi.queryEngine.model;

import net.aopacloud.superbi.queryEngine.enums.LogicalEnum;
import lombok.Data;

import java.util.List;

/**
 * @author: hudong
 * @date: 2023/8/14
 * @description:
 */
@Data
public class Filter{

    private String name;

    private String filterType;

    private String dataType;

    private LogicalEnum logical;

    private List<Condition> conditions;

    private boolean having = Boolean.FALSE;
}