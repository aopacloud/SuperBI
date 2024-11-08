package net.aopacloud.superbi.queryEngine;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.aopacloud.superbi.enums.EngineEnum;
import net.aopacloud.superbi.queryEngine.enums.QueryTypeEnum;
import net.aopacloud.superbi.queryEngine.executor.QueryExecutorFactory;
import net.aopacloud.superbi.queryEngine.model.QueryContext;
import net.aopacloud.superbi.queryEngine.model.QueryParam;
import net.aopacloud.superbi.queryEngine.model.QueryResult;
import net.aopacloud.superbi.queryEngine.schedule.PriorityQueryScheduler;
import net.aopacloud.superbi.queryEngine.schedule.QueryScheduler;
import net.aopacloud.superbi.queryEngine.sql.SqlAssembler;
import net.aopacloud.superbi.queryEngine.sql.SqlAssemblerFactory;
import net.aopacloud.superbi.queryEngine.sql.analytic.*;
import org.springframework.stereotype.Component;

import java.util.Map;

import static net.aopacloud.superbi.queryEngine.sql.AbstractSqlAssembler.checkTopN;

/**
 * Query execute engine. All query request will be handled by this engine.
 *
 * @author: hudong
 * @date: 2023/8/24
 * @description:
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class QueryExecuteEngine {

    private final QueryExecutorFactory queryExecutorFactory;

    private Map<EngineEnum, QueryScheduler> engineQuerySchedulerMap = Maps.newHashMap();

    public QueryResult execute(QueryContext queryContext) {

        if (!Strings.isNullOrEmpty(queryContext.getSql())) {
            log.info("sql: {}", queryContext.getSql());
            QueryScheduler queryScheduler = getQueryScheduler(queryContext.getEngine());
            return queryScheduler.submit(queryContext);
        }


        SqlAssembler sqlAssembler = SqlAssemblerFactory.getSqlAssembler(queryContext);

        AnalysisModel analysisModel = sqlAssembler.produce();
        String sql = analysisModel.getSql();
        queryContext.setSql(sql);

        QueryScheduler queryScheduler = getQueryScheduler(queryContext.getEngine());
        QueryResult result = queryScheduler.submit(queryContext);

        if (needQuerySummary(queryContext)) {
            QueryTypeEnum queryType = queryContext.getQueryParam().getType();
            if (queryType == QueryTypeEnum.QUERY) {
                QueryContext summaryContext = queryContext.clone();
                if (checkTopN(queryContext.getQueryParam())) {
                    TopNSummaryAnalysisModel topNSummaryAnalysisModel = new TopNSummaryAnalysisModel((TopNAnalysisModel) analysisModel);
                    topNSummaryAnalysisModel.setSummaryRow(queryContext.getQueryParam().getSummary());
                    topNSummaryAnalysisModel.setSummaryDetail(queryContext.getQueryParam().getSummaryDetail());
                    summaryContext.setSql(topNSummaryAnalysisModel.getSql());
                } else {
                    SummaryAnalysisModel summaryAnalysisModel = new SummaryAnalysisModel((QueryAnalysisModel) analysisModel);
                    summaryAnalysisModel.setSummaryRow(queryContext.getQueryParam().getSummary());
                    summaryAnalysisModel.setSummaryDetail(queryContext.getQueryParam().getSummaryDetail());
                    summaryContext.setSql(summaryAnalysisModel.getSql());
                }
                QueryResult summaryResult = queryScheduler.submit(summaryContext);
                result.setSummaryRows(summaryResult.getRows());
            }
            if (queryType == QueryTypeEnum.RATIO) {
                QueryContext summaryContext = queryContext.clone();
                if (checkTopN(queryContext.getQueryParam())) {
                    TopNRatioSummaryAnalysisModel topNRatioSummaryAnalysisModel = new TopNRatioSummaryAnalysisModel(analysisModel);
                    topNRatioSummaryAnalysisModel.setSummaryRow(queryContext.getQueryParam().getSummary());
                    topNRatioSummaryAnalysisModel.setSummaryDetail(queryContext.getQueryParam().getSummaryDetail());
                    summaryContext.setSql(topNRatioSummaryAnalysisModel.getSql());
                } else {
                    RatioSummaryAnalysisModel summaryAnalysisModel = new RatioSummaryAnalysisModel(analysisModel);
                    summaryAnalysisModel.setSummaryRow(queryContext.getQueryParam().getSummary());
                    summaryAnalysisModel.setSummaryDetail(queryContext.getQueryParam().getSummaryDetail());
                    summaryContext.setSql(summaryAnalysisModel.getSql());
                }
                QueryResult summaryResult = queryScheduler.submit(summaryContext);
                result.setSummaryRows(summaryResult.getRows());
            }
        }
        return result;
    }

    /**
     * Get query scheduler by engine. per engine has one scheduler.
     *
     * @param engine
     * @return
     */
    private QueryScheduler getQueryScheduler(EngineEnum engine) {

        QueryScheduler queryScheduler = engineQuerySchedulerMap.get(engine);

        if (queryScheduler == null) {
            synchronized (this) {
                queryScheduler = engineQuerySchedulerMap.get(engine);
                if (queryScheduler == null) {
                    queryScheduler = new PriorityQueryScheduler(queryExecutorFactory.getQueryExecutor(engine));
                    engineQuerySchedulerMap.put(engine, queryScheduler);
                }
            }
        }
        return queryScheduler;
    }

    private boolean needQuerySummary(QueryContext context) {

        QueryParam queryParam = context.getQueryParam();
        if (queryParam.getMeasures().isEmpty()) {
            return Boolean.FALSE;
        }

        return queryParam.getSummary() || queryParam.getSummaryDetail();
    }
}
