package net.aopacloud.superbi.queryEngine.sql.analytic;

import lombok.Data;
import lombok.experimental.Accessors;
import net.aopacloud.superbi.queryEngine.sql.Segment;

/**
 * @author: hudong
 * @date: 2023/10/25
 * @description:
 */
@Data
@Accessors(chain = true)
public class FieldCheckAnalysisModel implements AnalysisModel {

    private Segment field;

    private String table;

    private Segment where;

    private Segment groupBy;

    @Override
    public String getSql() {
        StringBuilder sql = new StringBuilder();
        sql.append("select ");
        if (groupBy != null) {
            sql.append(groupBy.getExpression()).append(" , ");
        }
        sql.append(field.getExpression());
        sql.append(" from ").append(table);
        sql.append(" where ").append(where.getExpression());

        if (groupBy != null) {
            sql.append(" group by ").append(groupBy.getExpression());
        }

        sql.append(" limit 100");

        return sql.toString();
    }
}
