package net.aopacloud.superbi.model.domain;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import lombok.Data;
import net.aopacloud.superbi.constant.BiConsist;
import net.aopacloud.superbi.model.dto.DatasetDTO;
import net.aopacloud.superbi.model.dto.DatasetFieldDTO;
import net.aopacloud.superbi.queryEngine.enums.LogicalEnum;
import net.aopacloud.superbi.queryEngine.model.Condition;
import net.aopacloud.superbi.queryEngine.model.Filter;
import net.aopacloud.superbi.queryEngine.sql.TypeConverter;
import net.aopacloud.superbi.queryEngine.sql.ck.ClickhouseTypeConverter;
import net.aopacloud.superbi.queryEngine.sql.operator.OperatorParam;
import net.aopacloud.superbi.queryEngine.util.TimeRangeParser;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
public class TableFilter {
    private String relation;
    private List<Item> children;

    public String parseFilterCondition(DatasetDTO dataset) {

        LogicalEnum relation = LogicalEnum.valueOf(getRelation().toUpperCase());

        List<String> wideExpression = getChildren().stream().map(item -> {

            LogicalEnum childRelation = LogicalEnum.valueOf(item.getRelation().toUpperCase());

            List<String> expressions = item.getChildren().stream().map(filter -> parseFilter(filter, dataset)).filter(Objects::nonNull).collect(Collectors.toList());
            return String.format(" ( %s ) ", Joiner.on(childRelation.getExpression()).join(expressions));
        }).collect(Collectors.toList());

        return Joiner.on(relation.getExpression()).join(wideExpression);
    }

    //组合过滤专用
    public String parseFilterConditionWithNestFilter(DatasetDTO dataset) {
        return String.format(" ( %s ) ", parseFilterCondition(dataset));
    }

    private String parseFilter(Filter filter, DatasetDTO dataset) {

        DatasetFieldDTO field = new DatasetFieldDTO();
        field.setName(filter.getName());
        field.setDataType(filter.getDataType());

        String latestPartitionValue = dataset.getLatestPartitionValue();

        List<String> conditionExpression = filter.getConditions().stream()
                .map(condition -> parseCondition(condition, filter.getName(), field, latestPartitionValue))
                .filter(item -> !Strings.isNullOrEmpty(item))
                .collect(Collectors.toList());

        if (conditionExpression.isEmpty()) {
            return null;
        }
        LogicalEnum logical = Objects.nonNull(filter.getLogical()) ? filter.getLogical() : LogicalEnum.AND;
        return String.format(" ( %s ) ", Joiner.on(logical.getExpression()).join(conditionExpression));
    }

    public String parseCondition(Condition condition, String expression, DatasetFieldDTO field, String latestPartitionValue) {

        OperatorParam param = new OperatorParam(condition.getArgs(), expression, field);

        // time
        if (field.getDataType().isTime()) {
            List<String> timeRange = TimeRangeParser.parseTimeRange(condition, latestPartitionValue)
                    .stream()
                    .map(time -> time.format(BiConsist.YYYY_MM_DD_FORMATTER))
                    .collect(Collectors.toList());
            param.setArgs(timeRange);
        }
        TypeConverter typeConverter = new ClickhouseTypeConverter();
        // text, number
        if (typeConverter.isDecimal(field.getDatabaseDataType())) {
            List<String> args = condition.getArgs().stream().map(arg -> typeConverter.toDecimal(arg)).collect(Collectors.toList());
            param.setArgs(args);
        }
        return condition.getFunctionalOperator().getOperator().apply(param);
    }

    @Data
    public static class Item {
        private String relation;
        private List<Filter> children;
    }
}
