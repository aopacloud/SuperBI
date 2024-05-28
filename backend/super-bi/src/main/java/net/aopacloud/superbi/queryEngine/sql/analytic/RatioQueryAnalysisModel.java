package net.aopacloud.superbi.queryEngine.sql.analytic;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.experimental.Accessors;
import net.aopacloud.superbi.constant.BiConsist;
import net.aopacloud.superbi.queryEngine.model.RatioMeasure;
import net.aopacloud.superbi.queryEngine.model.RatioPart;
import net.aopacloud.superbi.queryEngine.sql.Segment;

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

    protected String table;

    protected List<Segment> where = Lists.newArrayList();

    protected List<Segment> groupBy = Lists.newArrayList();

    protected List<Segment> having = Lists.newArrayList();

    protected List<Segment> orderBy = Lists.newArrayList();

    protected Segment paging;

    protected List<RatioPart> ratioParts;

    protected List<Segment> ratioDimensions;

    @Override
    public String getSql() {

        AnalysisModel originSubQuery = getOriginSubQuery();

        List<AnalysisModel> ratioSubQueries = ratioParts.stream().map(this::getRatioSubQuery).collect(Collectors.toList());

        // select
        List<String> outSideSelection = dimensions.stream().map(dim -> String.format("t1.%s", dim.getAlias())).collect(Collectors.toList());

        for (Segment measure: measures) {
            outSideSelection.add(String.format("t1.%s", measure.getAlias()));

            for(int i = 0; i < ratioParts.size(); i ++) {
                RatioPart part = ratioParts.get(i);
                Optional<RatioMeasure> ratioMeasure = part.getRatioMeasures().stream().filter(m -> measure.getAlias().equals(m.getId())).findFirst();
                if(ratioMeasure.isPresent()){
                    outSideSelection.add(String.format("t%d.%s as `%s`", i + 2, measure.getAlias(), ratioMeasure.get().getRatioAlias().replaceAll("`", "") ));
                }
            }
        }

        StringBuilder sql = new StringBuilder();
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
            sql.append(Joiner.on(" and ").join(onFields));
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

    protected AnalysisModel getRatioSubQuery(RatioPart part) {

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

        if (Objects.nonNull(ratioTimeRanges) && !ratioTimeRanges.isEmpty()) {
            ratioWhere.addAll(ratioTimeRanges);
        }

        QueryAnalysisModel ratioSubQuery = new QueryAnalysisModel()
                .addDimensions(ratioDimension).addMeasure(ratioSegments)
                .addWhere(ratioWhere)
                .addHaving(having)
                .addGroupBy(ratioDimension)
                .setTable(table)
                .setWithPaging(Boolean.FALSE);

        return ratioSubQuery;
    }


}
