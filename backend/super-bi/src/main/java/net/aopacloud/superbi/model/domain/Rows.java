package net.aopacloud.superbi.model.domain;

import com.google.common.base.Joiner;
import lombok.Data;
import net.aopacloud.superbi.model.dto.DatasetDTO;
import net.aopacloud.superbi.model.dto.DatasetFieldDTO;
import net.aopacloud.superbi.queryEngine.enums.LogicalEnum;
import net.aopacloud.superbi.queryEngine.sql.operator.FunctionalOperatorEnum;
import net.aopacloud.superbi.queryEngine.sql.operator.OperatorParam;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class Rows {
    private String relation;
    private List<Row> children;

    public String parseRowPrivilege(DatasetDTO datasetDTO) {

        Map<String, DatasetFieldDTO> fieldMap = datasetDTO.getFields().stream().collect(Collectors.toMap(field -> field.getName(), field -> field));

        LogicalEnum relation = LogicalEnum.valueOf(getRelation().toUpperCase());

        List<String> wideExpression = getChildren().stream().map(row -> {

            LogicalEnum childRelation = LogicalEnum.valueOf(row.getRelation().toUpperCase());

            List<String> expressions = row.getChildren().stream().map(condition -> {
                List<String> value = condition.getValue();

                DatasetFieldDTO field = fieldMap.get(condition.getField());

                String fieldName = field.getType().isNewAdd() ? String.format("[%s]", field.getName()) : field.getName();

                OperatorParam param = new OperatorParam(value, fieldName, field);
                FunctionalOperatorEnum operator = condition.getOperator();
                if ("ENUM".equals(condition.getType())) {
                    operator = FunctionalOperatorEnum.IN;
                }
                String expression = operator.getOperator().apply(param);

                return String.format("( %s )", expression);
            }).collect(Collectors.toList());

            return String.format(" ( %s ) ", Joiner.on(childRelation.getExpression()).join(expressions));
        }).collect(Collectors.toList());
        return Joiner.on(relation.getExpression()).join(wideExpression);
    }

    @Data
    public static class Row {
        private String relation;
        private List<Condition> children;
    }
    @Data
    public static class Condition {
        private String dataType;
        private String field;
        private FunctionalOperatorEnum operator;
        private String type;
        private String databaseDataType;
        private List<String> value;
    }
}


