package net.aopacloud.superbi.queryEngine.sql.analytic;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.experimental.Accessors;
import net.aopacloud.superbi.queryEngine.sql.Segment;

import java.util.List;

/**
 * single field query model.
 *
 * @author: hudong
 * @date: 2023/8/18
 * @description:
 */
@Data
@Accessors(chain = true)
public class SingleFieldAnalysisModel implements AnalysisModel {

    private Segment field;

    private String table;

    private List<Segment> where = Lists.newArrayList();

    private Segment paging;

    @Override
    public String getSql() {
        StringBuilder sql = new StringBuilder();
        sql.append("select distinct ")
                .append(field.getExpression())
                .append(" from ")
                .append(table);

        if (where != null && !where.isEmpty()) {
            sql.append(" where ").append(Joiner.on(" and ").join(getExpressions(where, Segment::getExpression)));
        }
        sql.append(" order by ").append(field.getExpression());

        sql.append(" limit 1000 ");

        return sql.toString();
    }
}
