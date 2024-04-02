package net.aopacloud.superbi.queryEngine.model;

import lombok.Data;
import net.aopacloud.superbi.queryEngine.enums.TimeTypeEnum;
import net.aopacloud.superbi.queryEngine.sql.operator.FunctionalOperatorEnum;

import java.util.List;

/**
 * @author: hudong
 * @date: 2023/8/14
 * @description:
 */
@Data
public class Condition {

    private FunctionalOperatorEnum functionalOperator;

    private TimeTypeEnum timeType;

    private List<String> args;

    private List<String> timeParts;

    private boolean useLatestPartitionValue = Boolean.FALSE;

    public boolean argIsEmpty() {
        return args == null || args.isEmpty();
    }
}