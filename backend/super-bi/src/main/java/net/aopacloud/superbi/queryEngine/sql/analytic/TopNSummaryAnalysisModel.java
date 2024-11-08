package net.aopacloud.superbi.queryEngine.sql.analytic;


import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.experimental.Accessors;
import net.aopacloud.superbi.constant.BiConsist;
import net.aopacloud.superbi.queryEngine.sql.Segment;
import net.aopacloud.superbi.queryEngine.sql.join.Table;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * topN summary analysis model.
 *
 * @author: yan.zu
 * @date: 2024/8/29
 * @description:
 */
@Data
@Accessors(chain = true)
public class TopNSummaryAnalysisModel implements AnalysisModel{

    private List<Segment> dimensions = Lists.newArrayList();

    private List<Segment> measures = Lists.newArrayList();

    private List<Segment> selections = Lists.newArrayList();

    private Table table;

    private List<Segment> where = Lists.newArrayList();

    private List<Segment> groupBy = Lists.newArrayList();

    private List<Segment> having = Lists.newArrayList();

    private List<Segment> orderBy = Lists.newArrayList();

    private Integer topN;

    private List<Segment> sortGroup = Lists.newArrayList();

    private String sortType;

    private Segment sortField;

    private boolean summaryRow;

    private boolean summaryDetail;

    private String topNSql;

    public TopNSummaryAnalysisModel(TopNAnalysisModel query) {
        this.dimensions = query.getDimensions();
        this.measures = query.getMeasures();
        this.selections = query.getSelections();
        this.table = query.getTable();
        this.where = query.getWhere();
        this.groupBy = query.getGroupBy();
        this.having = query.getHaving();
        this.topN = query.getTopN();
        this.sortGroup = query.getSortGroup();
        this.sortType = query.getSortType();
        this.sortField = query.getSortField();
        //构建topN Sql, 汇总行不加limit
        query.setWithPaging(false);
        this.topNSql = query.getSqlWithNoMetric();
    }

    public TopNSummaryAnalysisModel(TopNAnalysisModel query, boolean summaryRow, boolean summaryDetail) {
        this(query);
        this.summaryRow = summaryRow;
        this.summaryDetail = summaryDetail;
    }

    @Override
    public String getSql() {
        if (onlySummaryRow()){
            return getOnlySummaryRowSql();
        }
        return getSummaryDetailSql();
    }

    private String getSummaryDetailSql() {
        StringBuilder sql = new StringBuilder();
        sql.append("select ");
        List<String> selectionExpressions = getSummaryColumns();

        selectionExpressions.addAll(getExpressions(measures, Segment::getExpressionWithAlias));
        sql.append(Joiner.on(" , ").join(selectionExpressions));
        sql.append(" from ").append(table.produce());

        if (!where.isEmpty()) {
            sql.append(" where ").append(Joiner.on(" and ").join(getExpressions(where, Segment::getExpression))).append(" and ");
        } else {
            sql.append(" where ");
        }
        sql.append("(" + Joiner.on(" , ").join(getExpressions(dimensions, Segment::getExpression)) + ") in ");
        sql.append("(" + this.topNSql + ")");

        if (!groupBy.isEmpty()) {
            String groupingSets = Joiner.on(",").join(getGroupingSets());
            sql.append(" group by ").append(String.format(" grouping sets (%s)", groupingSets));
        }

        if (!having.isEmpty()) {
            sql.append(" having ").append(Joiner.on(" and ").join(getExpressions(having, Segment::getExpression)));
        }

        if(getOrderBySegment().isPresent()) {
            sql.append(" order by ").append(getOrderBySegment().get().getExpression());
        }

        return sql.toString();
    }

    private String getOnlySummaryRowSql() {
        StringBuilder sql = new StringBuilder();
        sql.append("select ");
        List<String> selectionExpressions = dimensions.stream()
                .map(segment -> String.format("'%s' as %s", BiConsist.SUMMARY_FLAG, segment.getAlias()))
                .collect(Collectors.toList());
        selectionExpressions.addAll(getExpressions(measures, Segment::getExpressionWithAlias));
        sql.append(Joiner.on(" , ").join(selectionExpressions));
        sql.append(" from ").append(table.produce());

        if (!where.isEmpty()) {
            sql.append(" where ").append(Joiner.on(" and ").join(getExpressions(where, Segment::getExpression))).append(" and ");
        } else {
            sql.append(" where ");
        }
        sql.append("(" + Joiner.on(" , ").join(getExpressions(dimensions, Segment::getExpression)) + ") in ");
        sql.append("(" + this.topNSql + ")");
        return sql.toString();
    }


    private Optional<Segment> getSummaryHaving() {
        List<String> expressions = dimensions.stream().map(segment -> String.format("grouping(%s) = 1", segment.getExpression())).collect(Collectors.toList());
        if (expressions.isEmpty()) {
            return Optional.empty();
        }
        String groupingExpression = Joiner.on(" or ").join(expressions);

        return Optional.of(new Segment(String.format("(%s)", groupingExpression)));

    }

    /**
     * remove last dimension, last dimension do not compute summary
     * @return
     */
    private List<String> getSummaryColumns() {

        LinkedList<Segment> summaryDimensions = Lists.newLinkedList(dimensions);
        Segment lastDimension = summaryDimensions.removeLast();
        List<String> columns = summaryDimensions.stream()
                .map(segment ->
                        String.format(" if(grouping(%s) = 1 , '%s', toString(%s) ) as %s", segment.getExpression(), BiConsist.SUMMARY_FLAG,segment.getExpression(), segment.getAlias()))
                .collect(Collectors.toList());
        columns.add(String.format("'%s' as %s", BiConsist.SUMMARY_FLAG, lastDimension.getAlias()));
        return columns;
    }

    private Optional<Segment> getOrderBySegment() {
        LinkedList<Segment> summaryDimensions = Lists.newLinkedList(dimensions);
        summaryDimensions.removeLast();
        List<String> expressions = summaryDimensions.stream().map(segment -> String.format("grouping(%s) desc", segment.getExpression())).collect(Collectors.toList());
        if (expressions.isEmpty()) {
            return Optional.empty();
        }
        String orderExpression = Joiner.on(" , ").join(expressions);
        return Optional.of(new Segment(orderExpression));
    }

    /**
     * like : group by GROUPING SETS( (name,age))
     * @return
     */
    private List<String> getGroupingSets() {
        LinkedList<String> result = Lists.newLinkedList();
        LinkedList<String> groupColumns = Lists.newLinkedList(dimensions.stream().map(Segment::getExpression).collect(Collectors.toList()));
        while(!groupColumns.isEmpty()) {
            groupColumns.removeLast();
            String flatColumns = Joiner.on(",").join(groupColumns);
            result.add(String.format("(%s)", flatColumns));
        }

        if(onlySummaryDetail()) {
            result.removeLast();
        }

        return result;
    }

    private boolean onlySummaryRow() {
        return !this.summaryDetail && this.summaryRow;
    }

    private boolean onlySummaryDetail() {
        return this.summaryDetail && !this.summaryRow;
    }
}
