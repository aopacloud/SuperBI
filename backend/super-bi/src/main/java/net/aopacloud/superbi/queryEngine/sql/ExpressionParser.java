package net.aopacloud.superbi.queryEngine.sql;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import net.aopacloud.superbi.enums.FieldTypeEnum;
import net.aopacloud.superbi.model.dto.DatasetFieldDTO;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: hudong
 * @date: 2023/10/25
 * @description:
 */
public class ExpressionParser {

    private Map<String, DatasetFieldDTO> fieldMap;

    public ExpressionParser(Map<String, DatasetFieldDTO> fieldMap) {
        this.fieldMap = fieldMap;
    }

    public ExpressionParser(List<DatasetFieldDTO> fields) {
        this.fieldMap = fields.stream().collect(Collectors.toMap(DatasetFieldDTO::getName, field -> field));
    }

    public String parse(String expression) {
        if (!Strings.isNullOrEmpty(expression) && expression.contains("[") && expression.contains("]")) {
            List<String> fieldNames = extractFieldNames(expression);
            for (String fieldName : fieldNames) {
                DatasetFieldDTO replaceField = fieldMap.get(fieldName);
                if (replaceField != null) {
                    String replaceExpression = replaceField.getType() == FieldTypeEnum.ORIGIN ? replaceField.getName() : replaceField.getExpression();
                    String newExpression = expression.replaceAll(String.format("\\[%s\\]", fieldName), "(" + replaceExpression + ")");
                    return parse(newExpression);
                }
            }
            return expression;
        }
        return expression;
    }

    private List<String> extractFieldNames(String expression) {
        List<String> list = Lists.newArrayList();
        int start = 0;
        int startFlag = 0;
        int endFlag = 0;
        for (int i = 0; i < expression.length(); i++) {
            if (expression.charAt(i) == '[') {
                startFlag++;
                if (startFlag == endFlag + 1) {
                    start = i;
                }
            } else if (expression.charAt(i) == ']') {
                endFlag++;
                if (endFlag == startFlag) {
                    list.add(expression.substring(start + 1, i));
                }
            }
        }
        return list;
    }

}
