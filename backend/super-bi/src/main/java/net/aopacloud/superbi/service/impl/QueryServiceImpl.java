package net.aopacloud.superbi.service.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.aopacloud.superbi.common.core.context.LoginContextHolder;
import net.aopacloud.superbi.constant.BiConsist;
import net.aopacloud.superbi.enums.QueryLogTypeEnum;
import net.aopacloud.superbi.enums.QueryStatusEnum;
import net.aopacloud.superbi.i18n.LocaleMessages;
import net.aopacloud.superbi.i18n.MessageConsist;
import net.aopacloud.superbi.model.domain.ExcelBuilder;
import net.aopacloud.superbi.model.dto.*;
import net.aopacloud.superbi.model.query.DatasetFieldValueQueryParam;
import net.aopacloud.superbi.queryEngine.QueryExecuteEngine;
import net.aopacloud.superbi.queryEngine.enums.QueryTypeEnum;
import net.aopacloud.superbi.queryEngine.executor.QueryResultProcessor;
import net.aopacloud.superbi.queryEngine.model.*;
import net.aopacloud.superbi.service.*;
import net.aopacloud.superbi.util.DateUtils;
import net.aopacloud.superbi.util.JSONUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: hudong
 * @date: 2023/8/23
 * @description:
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QueryServiceImpl implements QueryService {

    private final QueryExecuteEngine executeEngine;

    private final DatasetService datasetService;

    private final MetaDataService metaDataService;

    private final DatasetQueryLogService logService;

    private final DatasetPrivilegeService privilegeService;

    @Override
    public QueryResult query(QueryParam queryParam) {

        log.info("query param: {}", queryParam);

        DatasetDTO dataset = getDataset(queryParam);

        ConnectionParamDTO connection = metaDataService.getDatasetConnection(dataset);

        QueryContext context = QueryContext.ofQuery(queryParam, dataset, connection);

        QueryResult result = doQuery(context);

        recordQueryLog(context, result, QueryLogTypeEnum.QUERY);

        return result;
    }


    @Override
    public void download(QueryParam queryParam, boolean all) {

        Paging paging = all ? new Paging(BiConsist.MAX_DOWNLOAD_NUM) : new Paging(BiConsist.MAX_QUERY_NUM);
        queryParam.setPaging(paging);

        DatasetDTO dataset = getDataset(queryParam);

        ConnectionParamDTO connection = metaDataService.getDatasetConnection(dataset);

        QueryContext context = QueryContext.ofDownload(queryParam, dataset, connection);

        QueryResult result = doQuery(context);

        if (!result.getStatus().isSuccess()) {
            return;
        }
        String fileName = String.format("%s.%s", DateUtils.getCurrentTime(), BiConsist.EXCEL_SUFFIX);
        ExcelBuilder excelBuilder = new ExcelBuilder(fileName);

        // set title
        List<String> titles = getTitles(context);
        excelBuilder.setTitle(titles);

        // write data
        excelBuilder.setData(result.getRows());

        // download
        excelBuilder.writeToResponse();

        recordQueryLog(context, result, QueryLogTypeEnum.DOWNLOAD);
    }

    @Override
    public QueryResult queryDatasetFieldValue(List<DatasetFieldValueQueryParam> datasetFieldValueQueryParams) {

        QueryResult queryResult = new QueryResult();

        if (Objects.isNull(datasetFieldValueQueryParams)) {
            log.warn("query condition is null");
            return queryResult;
        }

        Set<DatasetFieldValueQueryParam> duplicate = datasetFieldValueQueryParams.stream().collect(Collectors.toSet());

        Set<Object> enumValues = duplicate.stream()
                .map(this::datasetFileValueQueryToContext)
                .map(this::doQuery)
                .flatMap(result -> result.getRows().stream())
                .flatMap(data -> Arrays.stream(data))
                .filter(item -> !Objects.isNull(item) && !Strings.isNullOrEmpty(item.toString()))
                .map(Object::toString)
                .collect(Collectors.toSet());

        List<Object[]> rows = enumValues.stream().sorted().map(item -> new Object[]{item}).collect(Collectors.toList());

        queryResult.setRows(rows);
        queryResult.setStatus(QueryStatusEnum.SUCCESS);
        return queryResult;
    }

    private QueryContext datasetFileValueQueryToContext(DatasetFieldValueQueryParam query) {
        QueryParam queryParam = new QueryParam();
        queryParam.setType(QueryTypeEnum.SINGLE_FIELD);
        queryParam.setFromSource(BiConsist.DASHBOARD_FILTER_FROM);
        queryParam.setDatasetId(query.getDatasetId());
        DatasetDTO datasetDTO = getDataset(queryParam);
        Dimension dim = new Dimension();
        dim.setName(query.getFieldName());
        queryParam.setDimensions(Lists.newArrayList(dim));

        ConnectionParamDTO connection = metaDataService.getDatasetConnection(datasetDTO);

        return QueryContext.ofQuery(queryParam, datasetDTO, connection);
    }

    private QueryResult doQuery(QueryContext context) {

        QueryPrivilege queryPrivilege = privilegeService.checkQueryPrivilege(context.getQueryParam(), context.getDataset(), LoginContextHolder.getUsername());

        if (!queryPrivilege.isPass() && context.getQueryParam().getType().needPrivilege()) {
            log.info("user {} hasn't any privilege to query", LoginContextHolder.getUsername());

            return QueryResult.noPrivilege();
        }

        if (queryPrivilege.hasRowPrivilege()) {
            context.setRowPrivileges(Sets.newHashSet(queryPrivilege.getRowPrivileges()));
        }

        List<TablePartition> lastTenPartition = metaDataService.getLastTenPartition(context.getDataset());

        context.setLatestPartitions(lastTenPartition);

        QueryResult result = executeEngine.execute(context);

        result.setColumns(getColumns(context));

        QueryResultProcessor.process(result);

        return result;
    }


    private void recordQueryLog(QueryContext context, QueryResult result, QueryLogTypeEnum queryLogType) {

        QueryParam queryParam = context.getQueryParam();
        if (!queryParam.getType().needRecord()) {
            return;
        }
        DatasetQueryLogDTO log = new DatasetQueryLogDTO();
        log.setDatasetId(queryParam.getDatasetId());
        log.setFromSource(queryParam.getFromSource());
        log.setUsername(context.getUsername());
        log.setSql(context.getSql());
        log.setType(queryLogType);
        log.setStatus(result.getStatus());
        log.setReportId(queryParam.getReportId());
        log.setDataNum(result.getTotal());
        log.setErrorLog(result.getErrorLog());
        log.setElapsed(result.getElapsed());
        log.setQueryParam(JSONUtils.toJsonString(queryParam));
        log.setRemark(result.getQueryId());
        logService.saveQueryLog(log);
    }

    private DatasetDTO getDataset(QueryParam queryParam) {
        return Optional.ofNullable(queryParam.getDataset())
                .orElseGet(() -> datasetService.findOne(queryParam.getDatasetId()));
    }

    private List<String> getTitles(QueryContext queryContext) {
        return getColumns(queryContext).stream().map(QueryColumn::getDisplayName).collect(Collectors.toList());
    }

    private List<QueryColumn> getColumns(QueryContext context) {

        List<QueryColumn> columns = Lists.newArrayList();
        QueryParam queryParam = context.getQueryParam();
        Map<String, DatasetFieldDTO> fieldMap = context.getFieldMap();

        for (Dimension dim : queryParam.getDimensions()) {

            DatasetFieldDTO datasetFieldDTO = fieldMap.get(dim.getName());
            QueryColumn column = QueryColumn.of(datasetFieldDTO);

            if (!Strings.isNullOrEmpty(dim.getDisplayName())) {
                column.setDisplayName(dim.getDisplayName());
            }
            columns.add(column);
        }

        Set<String> ratioFieldNames = Sets.newHashSet();
        if (queryParam.getCompare() != null) {
            List<Measure> ratioMeasures = queryParam.getCompare().getMeasures();
            if (ratioMeasures != null && !ratioMeasures.isEmpty()) {
                ratioFieldNames = ratioMeasures.stream().map(Measure::getName).collect(Collectors.toSet());
            }
        }

        for (Measure measure : queryParam.getMeasures()) {

            DatasetFieldDTO datasetFieldDTO = fieldMap.get(measure.getName());
            QueryColumn column = QueryColumn.of(datasetFieldDTO);
            column.setAggregator(measure.getAggregator());
            if (!Strings.isNullOrEmpty(measure.getDisplayName())) {
                column.setDisplayName(measure.getDisplayName());
                columns.add(column);
                if (ratioFieldNames.contains(measure.getName())) {
                    QueryColumn ratioColumn = column.copy();
                    ratioColumn.setDisplayName(String.format("%s(%s)", measure.getDisplayName(), LocaleMessages.getMessage(MessageConsist.RATIO_TIPS)));
                    columns.add(ratioColumn);
                }
            } else {
                columns.add(column);
                if (ratioFieldNames.contains(measure.getName())) {
                    QueryColumn ratioColumn = column.copy();
                    ratioColumn.setDisplayName(String.format("%s(%s)", datasetFieldDTO.getDisplayName(), LocaleMessages.getMessage(MessageConsist.RATIO_TIPS)));
                    columns.add(ratioColumn);
                }
            }
        }
        return columns;

    }
}
