package net.aopacloud.superbi.queryEngine.sql.analytic;

import net.aopacloud.superbi.constant.BiConsist;
import net.aopacloud.superbi.queryEngine.model.Measure;
import net.aopacloud.superbi.queryEngine.sql.Segment;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ratio query analysis model.
 * @author: hudong
 * @date: 2023/8/17
 * @description:
 */
@Data
@Accessors(chain = true)
public class RatioQueryAnalysisModel  implements AnalysisModel{

    private List<Segment> dimension = Lists.newArrayList();

    private List<Segment> measures = Lists.newArrayList();

    private String table;

    private List<Segment> where = Lists.newArrayList();

    private List<Segment> groupBy = Lists.newArrayList();

    private List<Segment> having = Lists.newArrayList();

    private List<Segment> orderBy = Lists.newArrayList();

    private Segment paging;

    private Segment joinOnField;

    private Segment ratioTimeRange;

    private List<Measure> ratioMeasures;

    @Override
    public String getSql() {

        QueryAnalysisModel originSubQuery = new QueryAnalysisModel()
                .addDimensions(dimension).addMeasure(measures)
                .addWhere(where)
                .addHaving(having)
                .addGroupBy(dimension)
                .setTable(table)
                .setWithPaging(Boolean.FALSE);

        List<Segment> ratioDimension = dimension.stream().filter(dim -> !dim.getName().equals(joinOnField.getName())).collect(Collectors.toList());
        ratioDimension.add(joinOnField);

        Map<String, Segment> measureMap = measures.stream().collect(Collectors.toMap(m -> m.getAlias(), m -> m));
        List<Segment> ratioSegments = ratioMeasures.stream().map(ratioMeasure -> measureMap.get(ratioMeasure.getId())).filter(segment -> !Objects.isNull(segment)).collect(Collectors.toList());

        List<Segment> ratioWhere = where.stream()
//                .filter(item -> Strings.isNullOrEmpty(item.getName()) || !item.getName().equals(ratioTimeRange.getName()))
                .filter(item -> Objects.isNull(ratioTimeRange) || !ratioTimeRange.getName().equals(item.getName()))
                .collect(Collectors.toList());

        if(Objects.nonNull(ratioTimeRange)) {
            ratioWhere.add(ratioTimeRange);
        }

        QueryAnalysisModel ratioSubQuery = new QueryAnalysisModel()
                .addDimensions(ratioDimension).addMeasure(ratioSegments)
                .addWhere(ratioWhere)
                .addHaving(having)
                .addGroupBy(ratioDimension)
                .setTable(table)
                .setWithPaging(Boolean.FALSE);


        // select
        List<String> outSideSelection = dimension.stream().map(dim -> String.format("t1.%s", dim.getAlias())).collect(Collectors.toList());

        Set<String> ratioMeasureAliasSet = ratioSegments.stream().map(Segment::getAlias).collect(Collectors.toSet());

        for(Segment measure : measures) {
            outSideSelection.add(String.format("t1.%s", measure.getAlias()));
            if(ratioMeasureAliasSet.contains(measure.getAlias())){
                outSideSelection.add(String.format("t2.%s as `%s%s`", measure.getAlias(),measure.getAlias().replaceAll("`",""), BiConsist.RATIO_FIELD_SUFFIX));
            }
        }


        StringBuilder sql = new StringBuilder();
        sql.append("select ");
        sql.append(Joiner.on(" , ").join(outSideSelection));
        sql.append(" from ").append("( ").append(originSubQuery.getSql()).append(") t1 ");
        sql.append(" left join ").append("( ").append(ratioSubQuery.getSql()).append(") t2 ");
        sql.append(" on ");

        List<String> onFields = dimension.stream().map(dim -> String.format(" t1.%s = t2.%s ", dim.getAlias(), dim.getAlias())).collect(Collectors.toList());

        sql.append(Joiner.on(" and ").join(onFields));

        if(!Objects.isNull(paging)) {
            sql.append(" limit ").append(paging.getExpression());
        } else {
            sql.append(" limit ").append(BiConsist.MAX_QUERY_NUM);
        }

        return sql.toString();
    }



    private List<Segment> getSelection() {
        List<Segment> selection = Lists.newArrayList();
        selection.addAll(dimension);
        selection.addAll(measures);
        return selection;
    }
}
