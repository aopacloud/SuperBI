package net.aopacloud.superbi.queryEngine.sql.analytic;

import com.google.common.base.Joiner;
import lombok.Data;
import lombok.experimental.Accessors;
import net.aopacloud.superbi.queryEngine.sql.Segment;
import net.aopacloud.superbi.queryEngine.sql.join.Table;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: hudong
 * @date: 2023/10/25
 * @description:
 */
@Data
@Accessors(chain = true)
public class FieldCheckAnalysisModel implements AnalysisModel {

    private Segment field;

    private Table table;

    private Segment where;

    private List<Segment> groupBy;

    @Override
    public String getSql() {
        StringBuilder sql = new StringBuilder();
        sql.append("explain select ");
        sql.append(field.getExpression());
        sql.append(" from ").append(table.produce());
        if (groupBy != null && !groupBy.isEmpty()) {
            sql.append(" group by ");
            sql.append(Joiner.on(" , ").join(groupBy.stream().map(Segment::getExpression).collect(Collectors.toList())));
        }

        sql.append(" limit 100");

        return sql.toString();
    }
}
