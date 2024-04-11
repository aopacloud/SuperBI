package net.aopacloud.superbi.service.impl;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import net.aopacloud.superbi.enums.QueryStatusEnum;
import net.aopacloud.superbi.i18n.LocaleMessages;
import net.aopacloud.superbi.i18n.MessageConsist;
import net.aopacloud.superbi.mapper.DatasetFieldMapper;
import net.aopacloud.superbi.mapper.DatasetMapper;
import net.aopacloud.superbi.model.converter.DatasetFieldConverter;
import net.aopacloud.superbi.model.dto.ConnectionParamDTO;
import net.aopacloud.superbi.model.dto.DatasetDTO;
import net.aopacloud.superbi.model.dto.DatasetFieldDTO;
import net.aopacloud.superbi.model.entity.Dataset;
import net.aopacloud.superbi.model.entity.DatasetField;
import net.aopacloud.superbi.model.vo.DatasetFieldCheckResultVO;
import net.aopacloud.superbi.model.vo.DatasetFieldCheckVO;
import net.aopacloud.superbi.queryEngine.QueryExecuteEngine;
import net.aopacloud.superbi.queryEngine.model.QueryContext;
import net.aopacloud.superbi.queryEngine.model.QueryResult;
import net.aopacloud.superbi.queryEngine.sql.ExpressionParser;
import net.aopacloud.superbi.queryEngine.sql.Segment;
import net.aopacloud.superbi.queryEngine.sql.analytic.FieldCheckAnalysisModel;
import net.aopacloud.superbi.service.DatasetFieldService;
import net.aopacloud.superbi.util.DateUtils;
import net.aopacloud.superbi.util.FieldUtils;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author: hudong
 * @date: 2023/10/25
 * @description:
 */
@Service
@RequiredArgsConstructor
public class DatasetFieldServiceImpl implements DatasetFieldService {

    private final QueryExecuteEngine queryExecuteEngine;

    private final DatasetFieldMapper datasetFieldMapper;

    private final DatasetMapper datasetMapper;

    private final DatasetFieldConverter converter;

    @Override
    public DatasetFieldCheckResultVO check(DatasetFieldCheckVO datasetFieldCheck) {

        DatasetFieldCheckResultVO result = new DatasetFieldCheckResultVO();

        DatasetDTO dataset = datasetFieldCheck.getDataset();
        String expression = datasetFieldCheck.getExpression();

        FieldCheckAnalysisModel model = new FieldCheckAnalysisModel();
        ExpressionParser parser = new ExpressionParser(dataset.getFields());
        model.setField(new Segment(parser.parse(expression)));
        model.setTable(String.format("%s.%s", dataset.getConfig().getDbName(), dataset.getConfig().getTableName()));

        if (FieldUtils.hasAggregation(expression)) {
            model.setGroupBy(new Segment("dt"));
            result.setHasAggregation(true);
        }

        model.setWhere(new Segment(String.format("%s='%s'", "dt", DateUtils.getCurrentDate())));

        QueryContext context = new QueryContext();
        context.setSql(model.getSql());
        ConnectionParamDTO connection = new ConnectionParamDTO();
        connection.setEngine(dataset.getConfig().getEngine());
        context.setConnectionParam(connection);

        QueryResult queryResult = queryExecuteEngine.execute(context);
        result.setQueryId(queryResult.getQueryId());
        if (queryResult.getStatus() == QueryStatusEnum.SUCCESS) {
            result.setPass(Boolean.TRUE);
        } else {
            result.setPass(Boolean.FALSE);
            if (Strings.isNullOrEmpty(queryResult.getErrorLog())) {
                result.setMsg(LocaleMessages.getMessage(MessageConsist.ADD_FIELD_ERROR));
            } else {
                result.setMsg(queryResult.getErrorLog());
            }
        }

        return result;
    }

    @Override
    public List<DatasetFieldDTO> findIntersectionFields(Set<Long> datasetIds) {

        if (Objects.isNull(datasetIds) || datasetIds.isEmpty()) {
            return Lists.newArrayList();
        }

        List<DatasetField> lastIntersection = null;
        List<DatasetField> currentIntersection = Lists.newArrayList();
        for (Long datasetId : datasetIds) {
            Dataset dataset = datasetMapper.selectById(datasetId);
            List<DatasetField> datasetFields = datasetFieldMapper.selectByDatasetAndVersion(datasetId, dataset.getVersion());
            if (Objects.isNull(lastIntersection)) {
                lastIntersection = Lists.newArrayList();
                lastIntersection.addAll(datasetFields);
                currentIntersection.addAll(datasetFields);
            } else {
                currentIntersection = Lists.newArrayList();
                for (DatasetField field : datasetFields) {
                    if (lastIntersection.contains(field)) {
                        currentIntersection.add(field);
                    }
                }
                lastIntersection = currentIntersection;
            }
        }
        return converter.entityToDTOList(currentIntersection);
    }
}
