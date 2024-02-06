package net.aopacloud.superbi.queryEngine.executor;

import net.aopacloud.superbi.enums.EngineEnum;

public interface QueryExecutorFactory {

    QueryExecutor getQueryExecutor(EngineEnum engine);

}
