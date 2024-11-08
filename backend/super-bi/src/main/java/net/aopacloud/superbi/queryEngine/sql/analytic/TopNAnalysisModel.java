package net.aopacloud.superbi.queryEngine.sql.analytic;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.experimental.Accessors;
import net.aopacloud.superbi.constant.BiConsist;
import net.aopacloud.superbi.queryEngine.sql.Segment;
import net.aopacloud.superbi.queryEngine.sql.join.Table;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * topN analysis model.
 *
 * @author: yan.zu
 * @date: 2024/8/29
 * @description:
 */
@Data
@Accessors(chain = true)
public class TopNAnalysisModel implements AnalysisModel {

    private List<Segment> dimensions = Lists.newArrayList();

    private List<Segment> measures = Lists.newArrayList();

    private List<Segment> selections = Lists.newArrayList();

    private Table table;

    private List<Segment> where = Lists.newArrayList();

    private List<Segment> groupBy = Lists.newArrayList();

    private List<Segment> having = Lists.newArrayList();

    private List<Segment> orderBy = Lists.newArrayList();

    private Segment paging;

    private Integer topN;

    private List<Segment> sortGroup = Lists.newArrayList();

    private String sortType;

    private Segment sortField;

    private boolean withPaging = Boolean.TRUE;

    @Override
    public String getSql() {
        StringBuilder sql = new StringBuilder();
        StringBuilder topNSql = new StringBuilder();
        StringBuilder SourceSql = new StringBuilder();
        buildSourceSql(SourceSql);
        buildTopNSql(topNSql, SourceSql);
        buildSql(sql, topNSql);
        return sql.toString();
    }

    public String getSqlWithNoMetric() {
        StringBuilder sql = new StringBuilder();
        StringBuilder topNSql = new StringBuilder();
        StringBuilder SourceSql = new StringBuilder();
        buildSourceSql(SourceSql);
        buildTopNSql(topNSql, SourceSql);
        buildSqlWithNoMetric(sql, topNSql);
        return sql.toString();
    }

    private void buildSourceSql(StringBuilder SourceSql) {
        SourceSql.append("select ");
        SourceSql.append(Joiner.on(" , ").join(getExpressions(selections, Segment::getExpressionWithAlias)));

        SourceSql.append(" from ").append(table.produce());

        if (!where.isEmpty()) {
            SourceSql.append(" where ").append(Joiner.on(" and ").join(getExpressions(where, Segment::getExpression)));
        }
        if (!groupBy.isEmpty()) {
            SourceSql.append(" group by ").append(Joiner.on(" , ").join(getExpressions(groupBy, Segment::getExpression)));
        }
        if (!having.isEmpty()) {
            SourceSql.append(" having ").append(Joiner.on(" and ").join(getExpressions(having, Segment::getExpression)));
        }
    }

    private void buildTopNSql(StringBuilder topNSql, StringBuilder SourceSql) {
        topNSql.append("select ");
        topNSql.append(Joiner.on(" , ").join(getExpressions(selections.stream()
                .collect(Collectors.toList()), Segment::getAlias)));
        topNSql.append(", row_number() over (partition by ")
                .append(Joiner.on(" , ").join(getExpressions(sortGroup, Segment::getAlias)))
                .append(" order by " + sortField.getAlias() + " " + sortType)
                .append(") as rank_count");
        topNSql.append(" from ").append("(" + SourceSql + ") t_source");
    }

    private void buildSql(StringBuilder sql, StringBuilder topNSql) {
        sql.append("select ");
        sql.append(Joiner.on(" , ").join(getExpressions(selections.stream()
                .collect(Collectors.toList()), Segment::getAlias)));
        sql.append(" from ").append("(" + topNSql + ") t_topN");

        if (!where.isEmpty()) {
            sql.append(" where rank_count <= ").append(topN);
        }
        if (withPaging) {
            if (!Objects.isNull(paging)) {
                sql.append(" limit ").append(paging.getExpression());
            } else {
                sql.append(" limit ").append(BiConsist.DEFAULT_QUERY_NUM);
            }
        }
    }

    private void buildSqlWithNoMetric(StringBuilder sql, StringBuilder topNSql) {
        sql.append("select ");
        sql.append(Joiner.on(" , ").join(getExpressions(dimensions, Segment::getAlias)));
        sql.append(" from ").append("(" + topNSql + ") t2");

        if (!where.isEmpty()) {
            sql.append(" where rank_count <= ").append(topN);
        }
    }

    public TopNAnalysisModel addDimensions(List<Segment> dimension) {
        this.dimensions.addAll(dimension);
        this.selections.addAll(dimension);
        return this;
    }

    public TopNAnalysisModel addMeasure(List<Segment> measures) {
        this.measures.addAll(measures);
        this.selections.addAll(measures);
        return this;
    }

    public TopNAnalysisModel addWhere(List<Segment> segments) {
        this.where.addAll(segments);
        return this;
    }

    public TopNAnalysisModel addGroupBy(List<Segment> segments) {
        this.groupBy.addAll(segments);
        return this;
    }

    public TopNAnalysisModel addHaving(List<Segment> segments) {
        this.having.addAll(segments);
        return this;
    }

    public TopNAnalysisModel addOrderBy(List<Segment> segments) {
        this.orderBy.addAll(segments);
        return this;
    }

    public TopNAnalysisModel addSortGroup(List<Segment> sortGroup) {
        this.sortGroup.addAll(sortGroup);
        return this;
    }

    public TopNAnalysisModel addSortField(Segment sortField) {
        this.sortField = sortField;
        return this;
    }

    public TopNAnalysisModel addSortType(String sortType) {
        this.sortType = sortType;
        return this;
    }

    public TopNAnalysisModel addTopN(Integer topN) {
        this.topN = topN;
        return this;
    }

    public void noPaging() {
        this.withPaging = Boolean.FALSE;
    }
}
