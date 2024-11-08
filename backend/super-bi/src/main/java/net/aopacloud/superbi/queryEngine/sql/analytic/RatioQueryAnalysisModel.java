package net.aopacloud.superbi.queryEngine.sql.analytic;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.experimental.Accessors;
import net.aopacloud.superbi.constant.BiConsist;
import net.aopacloud.superbi.queryEngine.model.RatioMeasure;
import net.aopacloud.superbi.queryEngine.model.RatioPart;
import net.aopacloud.superbi.queryEngine.sql.Segment;
import net.aopacloud.superbi.queryEngine.sql.join.Table;
import org.assertj.core.util.Strings;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ratio query analysis model.
 *
 * @author: hudong
 * @date: 2023/8/17
 * @description:
 */
@Data
@Accessors(chain = true)
public class RatioQueryAnalysisModel implements AnalysisModel {

    protected List<Segment> dimensions = Lists.newArrayList();

    protected List<Segment> measures = Lists.newArrayList();

    protected Table table;

    protected List<Segment> where = Lists.newArrayList();

    protected List<Segment> groupBy = Lists.newArrayList();

    protected List<Segment> having = Lists.newArrayList();

    protected List<Segment> orderBy = Lists.newArrayList();

    protected Segment paging;

    protected List<RatioPart> ratioParts;

    protected List<Segment> ratioDimensions;

    protected List<RatioMeasure> ratioMeasures;

    protected String ratioTimeFieldName;

    @Override
    public String getSql() {

        AnalysisModel originSubQuery = getOriginSubQuery();

        List<AnalysisModel> ratioSubQueries = ratioParts.stream().map(part -> getRatioSubQuery(part, false)).collect(Collectors.toList());

        // select
        List<String> outSideSelection = dimensions.stream().map(dim -> String.format("t1.%s", dim.getAlias())).collect(Collectors.toList());

        for (Segment measure: measures) {
            outSideSelection.add(String.format("t1.%s", measure.getAlias()));

            for(RatioMeasure ratioMeasure: ratioMeasures) {
                if (ratioMeasure.getId().equals(measure.getAlias())) {
                    for(int i = 0; i < ratioParts.size(); i ++) {
                        RatioPart part = ratioParts.get(i);
                        Optional<RatioMeasure> ratioMeasureOpt = part.getRatioMeasures().stream().filter(m -> ratioMeasure.getRatioAlias().equals(m.getRatioAlias())).findFirst();
                        if(ratioMeasureOpt.isPresent()){
                            outSideSelection.add(String.format("t%d.%s as `%s`", i + 2, measure.getAlias(), ratioMeasureOpt.get().getRatioAlias().replaceAll("`", "") ));
                        }
                    }
                }
            }
        }

        StringBuilder sql = new StringBuilder();
        sql.append("set distributed_product_mode = 'global';");
        sql.append("select ");
        sql.append(Joiner.on(" , ").join(outSideSelection));
        sql.append(" from ");
        sql.append("( ").append(originSubQuery.getSql()).append(") t1 ");

        for(int i = 0 ; i < ratioSubQueries.size(); i++) {
            String tableAlias = String.format("t%d", i +2);
            AnalysisModel subQuery = ratioSubQueries.get(i);

            sql.append(" left join ").append("( ").append(subQuery.getSql()).append(") ").append(tableAlias);
            sql.append(" on ");

            List<String> onFields = ratioDimensions.stream().map(dim -> String.format(" t1.%s = %s.%s ", dim.getAlias(),tableAlias, dim.getAlias())).collect(Collectors.toList());
            if (!onFields.isEmpty()) {
                sql.append(Joiner.on(" and ").join(onFields));
            } else {
                sql.append(" 1=1 ");
            }
        }

        if (!Objects.isNull(paging)) {
            sql.append(" limit ").append(paging.getExpression());
        } else {
            sql.append(" limit ").append(BiConsist.DEFAULT_QUERY_NUM);
        }

        return sql.toString();
    }

    protected AnalysisModel getOriginSubQuery(){
        QueryAnalysisModel originSubQuery = new QueryAnalysisModel()
                .addDimensions(dimensions).addMeasure(measures)
                .addWhere(where)
                .addHaving(having)
                .addGroupBy(dimensions)
                .setTable(table)
                .setWithPaging(Boolean.FALSE);
        return originSubQuery;
    }

    protected AnalysisModel getRatioSubQuery(RatioPart part, boolean withJoinTableFilter) {

        Segment joinOnSegment = part.getJoinOnSegment();

        Set<String> ratioDimSet = ratioDimensions.stream().map(Segment::getName).collect(Collectors.toSet());

        List<Segment> ratioDimension = dimensions.stream().map(dim -> dim.getName().equals(joinOnSegment.getName()) ? joinOnSegment : dim)
                .filter(dim -> ratioDimSet.contains(dim.getName()))
                .collect(Collectors.toList());

        Map<String, Segment> measureMap = measures.stream().collect(Collectors.toMap(m -> m.getAlias(), m -> m));
        List<Segment> ratioSegments = part.getRatioMeasures().stream().map(ratioMeasure -> measureMap.get(ratioMeasure.getId())).filter(segment -> !Objects.isNull(segment)).collect(Collectors.toList());

        List<Segment> ratioTimeRanges = part.getTimeRanges();

        Set<String> ratioTimeFieldSet = ratioTimeRanges.stream().map(Segment::getName).collect(Collectors.toSet());

        List<Segment> ratioWhere = where.stream()
                .filter(item -> !ratioTimeFieldSet.contains(item.getName()))
                .collect(Collectors.toList());

        Table subTable = withJoinTableFilter ? getFilterJoinTable(ratioTimeRanges) : table.clone();

        if (Objects.nonNull(ratioTimeRanges) && !ratioTimeRanges.isEmpty()) {
            ratioWhere.addAll(ratioTimeRanges);
            subTable.replacePushDownFilter(ratioTimeRanges.stream().findFirst().get());
        }

        QueryAnalysisModel ratioSubQuery = new QueryAnalysisModel()
                .addDimensions(ratioDimension).addMeasure(ratioSegments)
                .addWhere(ratioWhere)
                .addHaving(having)
                .addGroupBy(ratioDimension)
                .setTable(subTable)
                .setWithPaging(Boolean.FALSE);

        return ratioSubQuery;
    }

    public Table getFilterJoinTable(List<Segment> ratioTimeRanges) {
        StringBuilder tableSql = new StringBuilder();

        Table filterTable = table.clone();
        if (Objects.nonNull(ratioTimeRanges) && !ratioTimeRanges.isEmpty()) {
            filterTable.replacePushDownFilter(ratioTimeRanges.stream().findFirst().get());
        }

        String tmpTableSql = filterTable.produce();
        if (tmpTableSql.endsWith(BiConsist.JOIN_TABLE_ALIAS)) {
            tmpTableSql = tmpTableSql.substring(0, tmpTableSql.length() - BiConsist.JOIN_TABLE_ALIAS.length());
        }
        tableSql.append(tmpTableSql).append(" tt");

        List<Segment> selectedDimensions = dimensions.stream()
                .filter(segment -> !segment.getName().equals(ratioTimeFieldName))
                .map(segment -> new Segment(segment.getName(), segment.getAggregator(), segment.getExpression(), String.format("`%s%s`", segment.getName(), BiConsist.INNER_ALIAS)))
                .collect(Collectors.toList());

        if(!selectedDimensions.isEmpty()) {
            QueryAnalysisModel originSubQuery = new QueryAnalysisModel()
                    .addDimensions(selectedDimensions)
                    .addWhere(where)
                    .addHaving(having)
                    .addGroupBy(selectedDimensions)
                    .setTable(filterTable)
                    .setWithPaging(Boolean.FALSE);

            tableSql.append(" join (").append(originSubQuery.getSql()).append(" ) tm");
            List<String> joinOn = selectedDimensions.stream().map(segment -> String.format("%s = tm.`%s%s`", segment.getExpression(), segment.getName(), BiConsist.INNER_ALIAS)).collect(Collectors.toList());
            if (!joinOn.isEmpty()) {
                tableSql.append(" on ").append(Joiner.on(" and ").join(joinOn));
            }
        }
        return Table.builder().table(tableSql.toString()).build();
    }
}
