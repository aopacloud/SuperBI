package net.aopacloud.superbi.controller;

import lombok.RequiredArgsConstructor;
import net.aopacloud.superbi.common.core.web.domain.RestApiResponse;
import net.aopacloud.superbi.model.query.DatasetFieldValueQueryParam;
import net.aopacloud.superbi.queryEngine.model.QueryParam;
import net.aopacloud.superbi.queryEngine.model.QueryResult;
import net.aopacloud.superbi.service.QueryService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Query Center
 *
 * @author: hudong
 * @date: 2023/8/23
 * @description:
 */
@RestController
@RequiredArgsConstructor
public class QueryController {

    private final QueryService queryService;

    /**
     * query data
     *
     * @param queryParam
     * @return
     */
    @PostMapping("/query")
    public RestApiResponse<QueryResult> query(@RequestBody QueryParam queryParam) {
        QueryResult result = queryService.query(queryParam);
        return RestApiResponse.success(result);
    }

    /**
     * download data
     *
     * @param queryParam
     * @return
     */
    @PostMapping("/download")
    public void download(@RequestBody QueryParam queryParam, @RequestParam(defaultValue = "false") boolean all) {
        queryService.download(queryParam, all);
    }

    @PostMapping("/query/dashboard/filter")
    public RestApiResponse<QueryResult> queryDashboardFilter(@RequestBody List<DatasetFieldValueQueryParam> datasetFieldValueQueryParams) {
        QueryResult result = queryService.queryDatasetFieldValue(datasetFieldValueQueryParams);
        return RestApiResponse.success(result);
    }

}
