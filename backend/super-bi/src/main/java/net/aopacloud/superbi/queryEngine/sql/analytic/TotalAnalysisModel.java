package net.aopacloud.superbi.queryEngine.sql.analytic;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.experimental.Accessors;
import net.aopacloud.superbi.queryEngine.sql.Segment;
import net.aopacloud.superbi.queryEngine.sql.join.Table;

import java.util.List;

/**
 * query total count model.
 *
 * @author: hudong
 * @date: 2023/8/17
 * @description:
 */
@Data
@Accessors(chain = true)
public class TotalAnalysisModel implements AnalysisModel {

    private Table table;

    private List<Segment> where = Lists.newArrayList();

    private List<Segment> groupBy = Lists.newArrayList();

    private List<Segment> having = Lists.newArrayList();

    @Override
    public String getSql() {

        if (groupBy.isEmpty()) {
            return "select 1";
        }

        StringBuilder sql = new StringBuilder();

        sql.append("select count(*) from ");

        sql.append("( select ").append(Joiner.on(" , ").join(getExpressions(groupBy, Segment::getExpressionWithAlias)));

        sql.append(" from ").append(table.produce());

        if (!where.isEmpty()) {
            sql.append(" where ");
            sql.append(Joiner.on(" and ").join(getExpressions(where, Segment::getExpression)));
        }

        if (!groupBy.isEmpty()) {
            sql.append(" group by ");
            sql.append(Joiner.on(" , ").join(getExpressions(groupBy, Segment::getExpression)));
        }

        if (!having.isEmpty()) {
            sql.append(" having ");
            sql.append(Joiner.on(" and ").join(getExpressions(having, Segment::getExpression)));
        }

        sql.append(" ) t");

        sql.append(" limit 1");

        return sql.toString();
    }
}
