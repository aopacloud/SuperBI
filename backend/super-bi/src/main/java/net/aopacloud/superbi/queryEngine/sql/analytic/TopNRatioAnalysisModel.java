package net.aopacloud.superbi.queryEngine.sql.analytic;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import net.aopacloud.superbi.common.core.utils.bean.BeanUtils;
import net.aopacloud.superbi.queryEngine.sql.Segment;

import java.util.List;

/**
 * topN analysis model.
 *
 * @author: yan.zu
 * @date: 2024/8/29
 * @description:
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class TopNRatioAnalysisModel extends RatioQueryAnalysisModel {

    private Integer topN;

    private List<Segment> sortGroup = Lists.newArrayList();

    private String sortType;

    private Segment sortField;

    public TopNRatioAnalysisModel(AnalysisModel query) {
        BeanUtils.copyBeanProp(this, query);
    }

    @Override
    protected AnalysisModel getOriginSubQuery() {
        TopNAnalysisModel topNAnalysisModel =  new TopNAnalysisModel().addDimensions(dimensions).addMeasure(measures)
                .addWhere(where)
                .addHaving(having)
                .addGroupBy(dimensions)
                .setTable(table)
                .setPaging(paging)
                .setTopN(topN)
                .setSortField(sortField)
                .setSortType(sortType)
                .setSortGroup(sortGroup)
                .setWithPaging(Boolean.FALSE);

        return topNAnalysisModel;
    }


}
