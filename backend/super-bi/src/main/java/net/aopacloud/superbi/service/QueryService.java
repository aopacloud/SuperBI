package net.aopacloud.superbi.service;

import net.aopacloud.superbi.model.query.DashboardFilterQuery;
import net.aopacloud.superbi.queryEngine.model.QueryParam;
import net.aopacloud.superbi.queryEngine.model.QueryResult;

import java.util.List;

/**
 * Query service. All query request will be handled by this service.
 */
public interface QueryService {

    QueryResult query(QueryParam queryParam);

    void download(QueryParam queryParam, boolean all);

    QueryResult queryDashboardFilter(List<DashboardFilterQuery> dashboardFilterQueries);
}
