package net.aopacloud.superbi.service.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import net.aopacloud.superbi.enums.FieldTypeEnum;
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
import net.aopacloud.superbi.queryEngine.parser.CheckResult;
import net.aopacloud.superbi.queryEngine.parser.SqlColumns;
import net.aopacloud.superbi.queryEngine.parser.SqlParser;
import net.aopacloud.superbi.queryEngine.parser.SqlParserImpl;
import net.aopacloud.superbi.queryEngine.sql.*;
import net.aopacloud.superbi.queryEngine.sql.analytic.FieldCheckAnalysisModel;
import net.aopacloud.superbi.service.DatasetFieldService;
import net.aopacloud.superbi.service.MetaDataService;
import net.aopacloud.superbi.util.DateUtils;
import net.aopacloud.superbi.util.FieldUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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

    private final MetaDataService metaDataService;

    private final SqlParser sqlParser = new SqlParserImpl();

    @Override
    public DatasetFieldCheckResultVO check(DatasetFieldCheckVO datasetFieldCheck) {

        DatasetFieldCheckResultVO result = new DatasetFieldCheckResultVO();

        DatasetDTO dataset = datasetFieldCheck.getDataset();
        String expression = datasetFieldCheck.getExpression();

        ConnectionParamDTO connection = metaDataService.getDatasetConnection(dataset);
        TypeConverter typeConverter = TypeConverterFactory.getTypeConverter(connection.getEngine());
        QueryContext context = new QueryContext();
        context.setConnectionParam(connection);
        context.setDataset(dataset);

        SqlAssembler sqlAssembler = SqlAssemblerFactory.getSqlAssembler(context);

        FieldCheckAnalysisModel model = new FieldCheckAnalysisModel();
        ExpressionParser parser = new ExpressionParser(dataset.getFields(), typeConverter);
        DatasetFieldDTO fieldDTO = new DatasetFieldDTO();
        fieldDTO.setExpression(expression);
        fieldDTO.setType(FieldTypeEnum.ADD);
        String originExpression = parser.parse(fieldDTO);
        model.setField(new Segment(originExpression));
        model.setTable(sqlAssembler.getTable());

//        String unfoldExpression = sqlParser.removeOutsideAggregator(originExpression);
        String unfoldExpression = originExpression;
        if (sqlParser.hasAggregator(unfoldExpression)) {
            String sqlPart = String.format("select %s", unfoldExpression);
//            CheckResult checkResult = sqlParser.checkSql(sqlPart);
//            if(!checkResult.isPass()) {
//                result.setMsg(checkResult.getError());
//                return result;
//            }

            SqlColumns sqlColumns = sqlParser.parseSqlColumns(sqlPart);
            List<Segment> groupBy = sqlColumns.groupByColumns().stream().map(Segment::new).collect(Collectors.toList());

            model.setGroupBy(groupBy);
            result.setHasAggregation(true);
        }

        context.setSql(model.getSql());
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
                lastIntersection = Lists.newArrayList(datasetFields);
                currentIntersection = Lists.newArrayList(datasetFields);
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
