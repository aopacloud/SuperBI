package net.aopacloud.superbi.queryEngine.sql.analytic;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.experimental.Accessors;
import net.aopacloud.superbi.queryEngine.sql.Segment;

import java.util.List;
import java.util.Objects;

/**
 * @author: hudong
 * @date: 2023/10/26
 * @description:
 */
@Data
@Accessors(chain = true)
public class DatasetPreviewAnalysisModel implements AnalysisModel {

    private String table;

    private List<Segment> dimensions;

    private List<Segment> measures;

    private Segment where;

    @Override
    public String getSql() {
        StringBuilder sql = new StringBuilder();
        sql.append("select ");
        List<Segment> fields = Lists.newArrayList();
        fields.addAll(dimensions);
        fields.addAll(measures);
        sql.append(Joiner.on(" , ").join(getExpressions(fields, Segment::getExpressionWithAlias)));

//        sql.append(" from ").append(table);

        sql.append(" from ").append(" (  select * from ").append(table);

        if (where != null) {
            sql.append(" where ").append(where.getExpression());
        }

        sql.append(" limit 200 ) t");

        if (!Objects.isNull(dimensions) && !dimensions.isEmpty()) {
            sql.append(" group by ").append(Joiner.on(" , ").join(getExpressions(dimensions, Segment::getExpression)));
        }
        sql.append(" limit 100");

        return sql.toString();
    }
}
