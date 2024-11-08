package net.aopacloud.superbi.queryEngine.sql.analytic;

import lombok.Data;
import lombok.experimental.Accessors;
import net.aopacloud.superbi.common.core.utils.bean.BeanUtils;
import net.aopacloud.superbi.queryEngine.model.RatioPart;

/**
 * topN analysis model.
 *
 * @author: yan.zu
 * @date: 2024/8/29
 * @description:
 */
@Data
@Accessors(chain = true)
public class TopNRatioSummaryAnalysisModel extends TopNRatioAnalysisModel {

    private boolean summaryRow;

    private boolean summaryDetail;

    public TopNRatioSummaryAnalysisModel(AnalysisModel query) {
        BeanUtils.copyBeanProp(this, query);
    }

    @Override
    protected AnalysisModel getRatioSubQuery(RatioPart part, boolean withJoinTableFilter) {

        QueryAnalysisModel queryAnalysisModel = (QueryAnalysisModel) super.getRatioSubQuery(part, true);

        return new SummaryAnalysisModel(queryAnalysisModel, summaryRow, summaryDetail);
    }

    @Override
    protected AnalysisModel getOriginSubQuery() {
        TopNAnalysisModel topNAnalysisModel = (TopNAnalysisModel) super.getOriginSubQuery();

        return new TopNSummaryAnalysisModel(topNAnalysisModel, summaryRow, summaryDetail);
    }


}
