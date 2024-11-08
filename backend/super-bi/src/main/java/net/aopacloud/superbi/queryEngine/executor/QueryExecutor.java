package net.aopacloud.superbi.queryEngine.executor;

import net.aopacloud.superbi.enums.EngineEnum;
import net.aopacloud.superbi.queryEngine.model.QueryContext;
import net.aopacloud.superbi.queryEngine.model.QueryResult;

public interface QueryExecutor {

    QueryResult execute(QueryContext queryContext);

}
