package net.aopacloud.superbi.queryEngine.schedule;

import net.aopacloud.superbi.queryEngine.model.QueryContext;
import net.aopacloud.superbi.queryEngine.model.QueryResult;

/**
 * @author: hudong
 * @date: 2023/8/24
 * @description:
 */
public interface QueryScheduler {

    QueryResult submit(QueryContext queryContext);

}
