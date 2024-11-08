package net.aopacloud.superbi.model.domain;


import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.Data;
import net.aopacloud.superbi.enums.JoinTypeEnum;
import org.assertj.core.condition.Join;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class TableJoinDescriptor {

    private String leftTableAlias;

    private String rightTableAlias;

    private JoinTypeEnum joinType;

    private List<JoinField> joinFields;

    public List<String> getLeftJoinFieldsName() {
        return joinFields.stream().map(JoinField::getLeftFieldName).collect(Collectors.toList());
    }

    public List<String> getRightJoinFieldsName() {
        return joinFields.stream().map(JoinField::getRightFieldName).collect(Collectors.toList());
    }

    public List<String> getJoinFieldByAlias(String tableAlias) {

        if (leftTableAlias.equals(tableAlias)) {
            return getLeftJoinFieldsName();
        }

        if (rightTableAlias.equals(tableAlias)) {
            return getRightJoinFieldsName();
        }

        return Lists.newArrayList();
    }

    public String getJoinExpression() {
        List<String> onItems = joinFields.stream()
                .map(field -> String.format("%s.%s = %s.%s", leftTableAlias, field.getLeftFieldName(), rightTableAlias, field.getRightFieldName()))
                .collect(Collectors.toList());

        return Joiner.on(" and ").join(onItems);
    }
}
