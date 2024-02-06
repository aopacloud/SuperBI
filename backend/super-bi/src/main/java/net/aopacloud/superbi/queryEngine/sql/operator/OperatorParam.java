package net.aopacloud.superbi.queryEngine.sql.operator;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.aopacloud.superbi.enums.DataTypeEnum;
import net.aopacloud.superbi.model.dto.DatasetFieldDTO;
import org.apache.commons.compress.utils.Lists;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: hudong
 * @date: 2023/8/21
 * @description:
 */
@Data
@AllArgsConstructor
public class OperatorParam {

    private List<String> args;

    private String expression;

    private DatasetFieldDTO field;

    public List<String> getArgs() {
        if (args == null) {
            return Lists.newArrayList();
        }
        return args.stream()
                .map(arg -> arg.replaceAll("'", "''")) // 转义单引号
                .collect(Collectors.toList());
    }

    public boolean argIsEmpty() {
        return args == null || args.isEmpty();
    }

    public DataTypeEnum getDateType() {
        return field.getDataType();
    }

}
