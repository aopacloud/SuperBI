package net.aopacloud.superbi.queryEngine.sql;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import net.aopacloud.superbi.common.core.utils.StringUtils;
import net.aopacloud.superbi.enums.DataTypeEnum;
import net.aopacloud.superbi.enums.FieldTypeEnum;
import net.aopacloud.superbi.model.dto.DatasetFieldDTO;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author: hudong
 * @date: 2023/10/25
 * @description:
 */
public class ExpressionParser {

    private Map<String, DatasetFieldDTO> fieldMap;

    protected TypeConverter typeConverter;

    public ExpressionParser(Map<String, DatasetFieldDTO> fieldMap) {
        this.fieldMap = fieldMap;
    }

    public ExpressionParser(List<DatasetFieldDTO> fields, TypeConverter typeConverter) {
        this.fieldMap = fields.stream().collect(Collectors.toMap(DatasetFieldDTO::getName, field -> field));
        this.typeConverter = typeConverter;
    }

    public ExpressionParser(Map<String, DatasetFieldDTO> fieldMap, TypeConverter typeConverter) {
        this.fieldMap = fieldMap;
        this.typeConverter = typeConverter;
    }

    public String parse(DatasetFieldDTO field) {
        if (Objects.isNull(field)) {
            return StringUtils.EMPTY;
        }
        if (field.getType() == FieldTypeEnum.ORIGIN) {
            return switchDateType(String.format("`%s`", field.getName()), field);
        }
        String expression = field.getExpression();

        if (Strings.isNullOrEmpty(expression)) {
            return StringUtils.EMPTY;
        }

        return buildExpression(expression);
    }

    public String buildExpression(String expression) {
        if (!Strings.isNullOrEmpty(expression) && expression.contains("[") && expression.contains("]")) {
            List<String> fieldNames = extractFieldNames(expression);
            for (String fieldName : fieldNames) {
                DatasetFieldDTO replaceField = fieldMap.get(fieldName);
                if (replaceField != null) {
                    String replaceExpression = replaceField.getType() == FieldTypeEnum.ORIGIN ? switchDateType(String.format("`%s`", fieldName),replaceField) : replaceField.getExpression();
                    String newExpression = expression.replaceAll(String.format("\\[%s\\]", fieldName), "(" + replaceExpression + ")");
                    return buildExpression(newExpression);
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
    private String switchDateType(String expression, DatasetFieldDTO field) {

        if (field.getType().isNewAdd()) {
            return expression;
        }
        if (Objects.isNull(field.getOriginDataType())) {
            return expression;
        }
        if (field.getDataType() == field.getOriginDataType()) {
            return expression;
        }

        if (field.getDataType().isText()) {
            return typeConverter.toString(expression);
        }

        // to number
        if (field.getDataType() == DataTypeEnum.NUMBER) {
            // string to number
            if (field.getOriginDataType().isText()) {
                return typeConverter.stringToNumber(expression);
            }
            // time to number
            if (field.getOriginDataType().isTime()) {
                return typeConverter.timeToNumber(expression);
            }
        }
        // to time
        if (field.getDataType().isTime()) {
            // string to time
            if (field.getOriginDataType().isText()) {
                String convertExpression = typeConverter.stringToDate(expression);
                if (field.getDataType() == DataTypeEnum.TIME_YYYYMMDD_HHMMSS) {
                    convertExpression = typeConverter.stringToDateTime(expression);
                }
                return convertExpression;
            }

            // 数字转成时间
            if (field.getOriginDataType() == DataTypeEnum.NUMBER) {
                return typeConverter.numberToDate(expression);
            }
        }
        return expression;
    }
}
