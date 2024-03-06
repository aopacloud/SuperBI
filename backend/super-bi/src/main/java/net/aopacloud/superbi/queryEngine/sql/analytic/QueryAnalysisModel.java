package net.aopacloud.superbi.queryEngine.sql.analytic;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.experimental.Accessors;
import net.aopacloud.superbi.constant.BiConsist;
import net.aopacloud.superbi.queryEngine.sql.Segment;

import java.util.List;
import java.util.Objects;

/**
 * common query analysis model.
 *
 * @author: hudong
 * @date: 2023/8/17
 * @description:
 */
@Data
@Accessors(chain = true)
public class QueryAnalysisModel implements AnalysisModel {

    private List<Segment> dimensions = Lists.newArrayList();

    private List<Segment> measures = Lists.newArrayList();

    private List<Segment> selections = Lists.newArrayList();

    private String table;

    private List<Segment> where = Lists.newArrayList();

    private List<Segment> groupBy = Lists.newArrayList();

    private List<Segment> having = Lists.newArrayList();

    private List<Segment> orderBy = Lists.newArrayList();

    private Segment paging;

    private boolean withPaging = Boolean.TRUE;

    @Override
    public String getSql() {
        StringBuilder sql = new StringBuilder();
        sql.append("select ");
        sql.append(Joiner.on(" , ").join(getExpressions(selections, Segment::getExpressionWithAlias)));

        sql.append(" from ").append(table);

        if (!where.isEmpty()) {
            sql.append(" where ").append(Joiner.on(" and ").join(getExpressions(where, Segment::getExpression)));
        }
        if (!groupBy.isEmpty()) {
            sql.append(" group by ").append(Joiner.on(" , ").join(getExpressions(groupBy, Segment::getExpression)));
        }
        if (!having.isEmpty()) {
            sql.append(" having ").append(Joiner.on(" and ").join(getExpressions(having, Segment::getExpression)));
        }

        if (!orderBy.isEmpty()) {
            String orderByExpression = Joiner.on(" , ").join(getExpressions(orderBy, Segment::getExpression));
            sql.append(" order by ").append(orderByExpression);

            if (orderByExpression.contains("limit")) {
                noPaging();
            }
        } else {
            if (!measures.isEmpty()) {
                Segment orderByMeasure = measures.stream().findFirst().get();
                sql.append(" order by ").append(orderByMeasure.getExpression());
                sql.append(" desc ");
            }
        }
        if (withPaging) {
            if (!Objects.isNull(paging)) {
                sql.append(" limit ").append(paging.getExpression());
            } else {
                sql.append(" limit ").append(BiConsist.DEFAULT_QUERY_NUM);
            }
        }
        return sql.toString();
    }

    public QueryAnalysisModel addDimensions(List<Segment> dimension) {
        this.dimensions.addAll(dimension);
        this.selections.addAll(dimension);
        return this;
    }

    public QueryAnalysisModel addMeasure(List<Segment> measures) {
        this.measures.addAll(measures);
        this.selections.addAll(measures);
        return this;
    }

    public QueryAnalysisModel addWhere(List<Segment> segments) {
        this.where.addAll(segments);
        return this;
    }

    public QueryAnalysisModel addGroupBy(List<Segment> segments) {
        this.groupBy.addAll(segments);
        return this;
    }

    public QueryAnalysisModel addHaving(List<Segment> segments) {
        this.having.addAll(segments);
        return this;
    }

    public QueryAnalysisModel addOrderBy(List<Segment> segments) {
        this.orderBy.addAll(segments);
        return this;
    }

    public void noPaging() {
        this.withPaging = Boolean.FALSE;
    }
}
