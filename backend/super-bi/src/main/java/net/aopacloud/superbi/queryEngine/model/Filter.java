package net.aopacloud.superbi.queryEngine.model;

import lombok.Data;
import lombok.experimental.Accessors;
import net.aopacloud.superbi.enums.DataTypeEnum;
import net.aopacloud.superbi.queryEngine.enums.LogicalEnum;

import java.util.List;

/**
 * @author: hudong
 * @date: 2023/8/14
 * @description:
 */
@Data
@Accessors(chain = true)
public class Filter {

    private String name;

    private String filterType;

    private DataTypeEnum dataType;

    private LogicalEnum logical;

    private List<Condition> conditions;

    private boolean having = Boolean.FALSE;
}