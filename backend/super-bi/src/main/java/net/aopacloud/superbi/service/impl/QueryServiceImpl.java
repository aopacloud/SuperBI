package net.aopacloud.superbi.service.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.aopacloud.superbi.common.core.context.LoginContextHolder;
import net.aopacloud.superbi.constant.BiConsist;
import net.aopacloud.superbi.enums.FieldCategoryEnum;
import net.aopacloud.superbi.enums.FieldTypeEnum;
import net.aopacloud.superbi.enums.QueryLogTypeEnum;
import net.aopacloud.superbi.enums.QueryStatusEnum;
import net.aopacloud.superbi.model.domain.ExcelBuilder;
import net.aopacloud.superbi.model.dto.*;
import net.aopacloud.superbi.model.query.DatasetFieldValueQueryParam;
import net.aopacloud.superbi.queryEngine.QueryExecuteEngine;
import net.aopacloud.superbi.queryEngine.enums.DateTruncEnum;
import net.aopacloud.superbi.queryEngine.enums.QueryTypeEnum;
import net.aopacloud.superbi.queryEngine.executor.QueryResultProcessor;
import net.aopacloud.superbi.queryEngine.model.*;
import net.aopacloud.superbi.queryEngine.util.RatioLabelGenerator;
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

        Paging paging = all ? new Paging(BiConsist.MAX_DOWNLOAD_NUM) : new Paging(BiConsist.DEFAULT_QUERY_NUM);
        queryParam.setPaging(paging);

        DatasetDTO dataset = getDataset(queryParam);

        ConnectionParamDTO connection = metaDataService.getDatasetConnection(dataset);

        QueryContext context = QueryContext.ofDownload(queryParam, dataset, connection);

        QueryResult result = doQuery(context);

        if (!result.getStatus().isSuccess()) {
            return;
        }
        String fileName = String.format("%s.%s", DateUtils.getCurrentTime(), BiConsist.EXCEL_SUFFIX);
        ExcelBuilder excelBuilder = new ExcelBuilder(fileName, context.getColumns());

        // set title
        List<String> titles = context.getTitles();
        excelBuilder.setTitle(titles);

        // write data
        excelBuilder.setData(result.getRows());

        excelBuilder.autoColumnSize();

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
                .filter(Objects::nonNull)
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
        try {
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
        } catch (Exception e) {
            log.error("query filter value error", e);
        }
        return null;
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
        result.setDimensionColumnCount(context.getQueryParam().getDimensions().size());

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

        if (Objects.nonNull(queryParam.getDataset()) && Objects.nonNull(queryParam.getDataset().getFields())) {
            return queryParam.getDataset();
        }

        if (Objects.nonNull(queryParam.getDatasetId())) {
            return datasetService.findOne(queryParam.getDatasetId());
        }

        if (Objects.nonNull(queryParam.getDataset())) {

            DatasetDTO dataset = queryParam.getDataset();
            DatasetMetaConfigDTO config = dataset.getConfig();
            TableSchemaDTO schema = new TableSchemaDTO();
            schema.setEngine(config.getEngine());
            schema.setDbName(config.getDbName());
            schema.setTableName(config.getTableName());
            TableSchemaDTO tableSchema = metaDataService.getTableSchema(schema);

            List<DatasetFieldDTO> datasetFields = tableSchema.getFields().stream().map(field -> {
                DatasetFieldDTO datasetField = new DatasetFieldDTO();
                datasetField.setDisplayName(field.getDescription());
                datasetField.setName(field.getName());
                datasetField.setType(FieldTypeEnum.ORIGIN);
                datasetField.setDataType(field.getDataType());
                datasetField.setDatabaseDataType(field.getDatabaseDataType());
                return datasetField;
            }).collect(Collectors.toList());

            dataset.setFields(datasetFields);

            return dataset;

        }

        throw new IllegalArgumentException("dataset is null");

    }
}
