package net.aopacloud.superbi.queryEngine.sql.analytic;


import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.experimental.Accessors;
import net.aopacloud.superbi.constant.BiConsist;
import net.aopacloud.superbi.queryEngine.sql.Segment;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
@Accessors(chain = true)
public class SummaryAnalysisModel implements AnalysisModel{

    private List<Segment> dimensions = Lists.newArrayList();

    private List<Segment> measures = Lists.newArrayList();

    private List<Segment> selections = Lists.newArrayList();

    private String table;

    private List<Segment> where = Lists.newArrayList();

    private List<Segment> groupBy = Lists.newArrayList();

    private List<Segment> having = Lists.newArrayList();

    private List<Segment> orderBy = Lists.newArrayList();

    public SummaryAnalysisModel(QueryAnalysisModel query) {
        this.dimensions = query.getDimensions();
        this.measures = query.getMeasures();
        this.selections = query.getSelections();
        this.table = query.getTable();
        this.where = query.getWhere();
        this.groupBy = query.getGroupBy();
        this.having = query.getHaving();
    }

    @Override
    public String getSql() {
        StringBuilder sql = new StringBuilder();
        sql.append("select ");
        List<String> selectionExpressions = dimensions.stream()
                .map(segment ->
                    String.format(" if(grouping(%s) = 1 , '-', toString(%s) ) as %s", segment.getExpression(), segment.getExpression(), segment.getAlias()))
                .collect(Collectors.toList());

        selectionExpressions.addAll(getExpressions(measures, Segment::getExpressionWithAlias));

        sql.append(Joiner.on(" , ").join(selectionExpressions));

        sql.append(" from ").append(table);

        if (!where.isEmpty()) {
            sql.append(" where ").append(Joiner.on(" and ").join(getExpressions(where, Segment::getExpression)));
        }
        if (!groupBy.isEmpty()) {
            sql.append(" group by ").append(Joiner.on(" , ").join(getExpressions(groupBy, Segment::getExpression))).append(" with rollup ");
        }

        having.add(getSummaryHaving());

        if (!having.isEmpty()) {
            sql.append(" having ").append(Joiner.on(" and ").join(getExpressions(having, Segment::getExpression)));
        }

        Segment orderBySegment = getOrderBySegment();
        if(Objects.nonNull(orderBySegment)) {
            sql.append(" order by ").append(orderBySegment.getExpression());
        }

        return sql.toString();
    }


    private Segment getSummaryHaving() {
        List<String> expressions = dimensions.stream().map(segment -> String.format("grouping(%s) = 1", segment.getExpression())).collect(Collectors.toList());
        String groupingExpression = Joiner.on(" or ").join(expressions);
        return new Segment(String.format("(%s)", groupingExpression));
    }

    private Segment getOrderBySegment() {
        List<String> expressions = dimensions.stream().map(segment -> String.format("grouping(%s) desc", segment.getExpression())).collect(Collectors.toList());
        String orderExpression = Joiner.on(" , ").join(expressions);
        return new Segment(orderExpression);
    }
}
