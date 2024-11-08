package net.aopacloud.superbi.queryEngine.util;

import com.google.common.collect.Lists;
import net.aopacloud.superbi.queryEngine.enums.TimeTypeEnum;
import net.aopacloud.superbi.queryEngine.model.Condition;

import java.time.LocalDateTime;
import java.util.List;

public class TimeRangeParser {

    public static List<LocalDateTime> parseTimeRange(Condition condition, String latestPartitionValue) {

        if (condition.isUseLatestPartitionValue()) {
            return TimeTypeEnum.EXACT.getDateTime(Lists.newArrayList(latestPartitionValue, latestPartitionValue), Lists.newArrayList());
        }

        List<String> args = condition.getArgs();

        if (args.size() == 1) {
            args = Lists.newArrayList(args.get(0), args.get(0));
        }

        return condition.getTimeType().getDateTime(args, condition.getTimeParts());

    }

}
