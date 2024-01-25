package net.aopacloud.superbi.queryEngine;

import net.aopacloud.superbi.enums.EngineEnum;
import net.aopacloud.superbi.queryEngine.executor.QueryExecutorFactory;
import net.aopacloud.superbi.queryEngine.model.QueryContext;
import net.aopacloud.superbi.queryEngine.model.QueryResult;
import net.aopacloud.superbi.queryEngine.schedule.PriorityQueryScheduler;
import net.aopacloud.superbi.queryEngine.schedule.QueryScheduler;
import net.aopacloud.superbi.queryEngine.sql.SqlAssembler;
import net.aopacloud.superbi.queryEngine.sql.SqlAssemblerFactory;
import net.aopacloud.superbi.queryEngine.sql.analytic.AnalysisModel;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Query execute engine. All query request will be handled by this engine.
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

        if(Strings.isNullOrEmpty(queryContext.getSql())) {
            SqlAssembler sqlAssembler = SqlAssemblerFactory.getSqlAssembler(queryContext);

            AnalysisModel analysisModel = sqlAssembler.produce();
            String sql = analysisModel.getSql();
            queryContext.setSql(sql);
        }
        log.info("sql: {}", queryContext.getSql());

        QueryScheduler queryScheduler = getQueryScheduler(queryContext.getEngine());

        return queryScheduler.submit(queryContext);
    }

    /**
     * Get query scheduler by engine. per engine has one scheduler.
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
}
