package net.aopacloud.superbi.queryEngine.sql.analytic;

import lombok.Data;
import lombok.experimental.Accessors;
import net.aopacloud.superbi.common.core.utils.bean.BeanUtils;
import net.aopacloud.superbi.queryEngine.model.RatioPart;

/**
 * ratio query analysis model.
 *
 * @author: hudong
 * @date: 2023/8/17
 * @description:
 */
@Data
@Accessors(chain = true)
public class RatioSummaryAnalysisModel extends RatioQueryAnalysisModel {

    private boolean summaryRow;

    private boolean summaryDetail;

    public RatioSummaryAnalysisModel(AnalysisModel query) {
        BeanUtils.copyBeanProp(this, query);
    }

    @Override
    protected AnalysisModel getRatioSubQuery(RatioPart part, boolean withJoinTableFilter) {

        QueryAnalysisModel queryAnalysisModel = (QueryAnalysisModel) super.getRatioSubQuery(part, true);

        return new SummaryAnalysisModel(queryAnalysisModel, summaryRow, summaryDetail);
    }

    @Override
    protected AnalysisModel getOriginSubQuery() {
        QueryAnalysisModel queryAnalysisModel = (QueryAnalysisModel) super.getOriginSubQuery();

        return new SummaryAnalysisModel(queryAnalysisModel, summaryRow, summaryDetail);
    }


}
