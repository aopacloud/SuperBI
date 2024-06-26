package net.aopacloud.superbi.queryEngine.sql;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import net.aopacloud.superbi.common.core.exception.ServiceException;
import net.aopacloud.superbi.common.core.utils.StringUtils;
import net.aopacloud.superbi.constant.BiConsist;
import net.aopacloud.superbi.enums.DataTypeEnum;
import net.aopacloud.superbi.enums.FieldCategoryEnum;
import net.aopacloud.superbi.enums.FieldTypeEnum;
import net.aopacloud.superbi.i18n.LocaleMessages;
import net.aopacloud.superbi.i18n.MessageConsist;
import net.aopacloud.superbi.model.dto.DatasetDTO;
import net.aopacloud.superbi.model.dto.DatasetExtraConfigDTO;
import net.aopacloud.superbi.model.dto.DatasetFieldDTO;
import net.aopacloud.superbi.model.dto.TablePartition;
import net.aopacloud.superbi.queryEngine.TableMergeStage;
import net.aopacloud.superbi.queryEngine.enums.*;
import net.aopacloud.superbi.queryEngine.model.*;
import net.aopacloud.superbi.queryEngine.sql.aggregator.Aggregators;
import net.aopacloud.superbi.queryEngine.sql.analytic.*;
import net.aopacloud.superbi.queryEngine.sql.operator.FunctionalOperatorEnum;
import net.aopacloud.superbi.queryEngine.sql.operator.OperatorParam;
import net.aopacloud.superbi.util.FieldUtils;
import net.aopacloud.superbi.util.JSONUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: hudong
 * @date: 2023/8/18
 * @description:
 */
@Slf4j
public abstract class AbstractSqlAssembler implements SqlAssembler {

    protected DatasetDTO dataset;

    protected TypeConverter typeConverter;

    protected QueryContext queryContext;

    protected TableMergeStage tableStage;

    protected Map<String, DatasetFieldDTO> fieldMap;

    protected ExpressionParser expressionParser;

    public AbstractSqlAssembler(DatasetDTO dataset, TypeConverter typeConverter, TableMergeStage tableStage, QueryContext queryContext) {
        this.dataset = dataset;
        this.typeConverter = typeConverter;
        this.tableStage = tableStage;
        this.queryContext = queryContext;
        fieldMap = dataset.getFields().stream().collect(Collectors.toMap(DatasetFieldDTO::getName, field -> field));
        this.expressionParser = new ExpressionParser(fieldMap,typeConverter);
    }

    @Override
    public AnalysisModel produce() {

        QueryParam queryParam = queryContext.getQueryParam();

        List<Segment> dimensions = queryParam.getDimensions().stream().flatMap(dim -> parseDimension(dim).stream()).collect(Collectors.toList());
        List<Segment> measures = queryParam.getMeasures().stream().map(this::parseMeasure).filter(measure -> !Objects.isNull(measure)).collect(Collectors.toList());
        List<Segment> filters = queryParam.getFilters().stream().filter(filter -> !isHavingFilter(filter)).map(this::parseFilter).filter(filter -> !Objects.isNull(filter)).collect(Collectors.toList());
        List<Segment> having = queryParam.getFilters().stream().filter(filter -> isHavingFilter(filter)).map(filter -> parseHaving(filter, measures)).filter(filter -> !Objects.isNull(filter)).collect(Collectors.toList());
        List<Segment> orderBy = queryParam.getSorts().stream().map(sort -> parseSortBy(sort, measures)).collect(Collectors.toList());
        Segment paging = parsePaging(queryParam.getPaging());

        // parse row privilege
        List<Segment> rowPrivilege = parseRowPrivilege(queryContext.getRowPrivileges());
        filters.addAll(rowPrivilege);

        // parse default partition range
        List<Segment> defaultPartitionRange = parsePartitionRange(dataset, queryParam);
        filters.addAll(defaultPartitionRange);


        switch (queryParam.getType()) {
            case QUERY:
                return new QueryAnalysisModel().addDimensions(dimensions).addMeasure(measures)
                        .addWhere(filters)
                        .addHaving(having)
                        .addGroupBy(dimensions)
                        .setTable(getTable())
                        .setOrderBy(orderBy)
                        .setPaging(paging);

            case TOTAL:
                return new TotalAnalysisModel().setTable(getTable())
                        .setGroupBy(dimensions)
                        .setWhere(filters)
                        .setHaving(having);

            case SINGLE_FIELD:
                SingleFieldAnalysisModel model = new SingleFieldAnalysisModel()
                        .setTable(getTable())
                        .setField(dimensions.get(0))
                        .setPaging(paging);

                List<TablePartition> partitions = queryContext.getLatestPartitions();
                if (!Objects.isNull(partitions) && !partitions.isEmpty()) {
                    List<String> args = partitions.stream().map(TablePartition::getPartitionValue).collect(Collectors.toList());
                    DatasetFieldDTO field = getPartitionField();
                    if (!Objects.isNull(field)) {
//                        String expression = getFieldExpression(field);
                        OperatorParam param = new OperatorParam(args, field.getName(), field);
                        Segment where = new Segment(field.getName(), FunctionalOperatorEnum.IN.getOperator().apply(param));
                        model.setWhere(Lists.newArrayList(where));
                    }
                }

                return model;

            case RATIO:
                Compare compare = queryParam.getCompare();
                List<RatioPart> ratioParts = parseRatioPart(compare, queryParam, dimensions);
                List<Segment> ratioDimSegments = dimensions;
                if(Objects.nonNull(compare.getDimensions())) {
                    Set<String> ratioDimNameSet = compare.getDimensions().stream().map(Dimension::getName).collect(Collectors.toSet());
                    ratioDimSegments = dimensions.stream().filter(dim -> ratioDimNameSet.contains(dim.getName())).collect(Collectors.toList());
                }

                return new RatioQueryAnalysisModel().setDimensions(dimensions)
                        .setMeasures(measures)
                        .setWhere(filters)
                        .setHaving(having)
                        .setGroupBy(dimensions)
                        .setTable(getTable())
                        .setOrderBy(orderBy)
                        .setPaging(paging)
                        .setRatioParts(ratioParts)
                        .setRatioDimensions(ratioDimSegments);

            case PREVIEW:
                List<Segment> dimSegments = queryParam.getDataset().getFields().stream()
                        .filter(field -> field.getCategory() == FieldCategoryEnum.DIMENSION)
                        .map(field -> new Segment(field.getName(), expressionParser.parse(field)))
                        .collect(Collectors.toList());

                List<Segment> measureSegments = queryParam.getDataset().getFields().stream()
                        .filter(field -> field.getCategory() == FieldCategoryEnum.MEASURE)

                        .map(field -> new Segment(field.getName(), field.getAggregator(), Aggregators.of(field.getAggregator()).buildAggregationExpression(expressionParser.parse(field))))

                        .collect(Collectors.toList());

                DatasetPreviewAnalysisModel previewAnalysisModel = new DatasetPreviewAnalysisModel()
                        .setTable(getTable())
                        .setDimensions(dimSegments)
                        .setMeasures(measureSegments);

                List<TablePartition> latestPartitions = queryContext.getLatestPartitions();

                if (!Objects.isNull(latestPartitions) && !latestPartitions.isEmpty()) {
                    List<String> args = latestPartitions.stream().map(TablePartition::getPartitionValue).limit(1).collect(Collectors.toList());
                    DatasetFieldDTO field = getPartitionField();
                    if (!Objects.isNull(field)) {
//                        String expression = getFieldExpression(field);
                        OperatorParam param = new OperatorParam(args, field.getName(), field);
                        Segment where = new Segment(field.getName(), FunctionalOperatorEnum.IN.getOperator().apply(param));
                        previewAnalysisModel.setWhere(where);
                    }
                }

                return previewAnalysisModel;

            default:
                throw new IllegalArgumentException("not support query type");
        }
    }

    private List<Segment> parsePartitionRange(DatasetDTO dataset, QueryParam queryParam) {
        if(Strings.isNullOrEmpty(dataset.getExtraConfig())) {
            return Lists.newArrayList();
        }
        DatasetExtraConfigDTO extraConfig = JSONUtils.parseObject(dataset.getExtraConfig(), DatasetExtraConfigDTO.class);

        if(Objects.isNull(extraConfig) || Objects.isNull(extraConfig.getPartitionRanges()) || extraConfig.getPartitionRanges().isEmpty()) {
            return Lists.newArrayList();
        }

        List<Filter> partitionRanges = extraConfig.getPartitionRanges();
        List<Filter> filters = queryParam.getFilters();
        for(Filter partitionRange : partitionRanges) {
            for(Filter filter : filters) {
                verifyPartitionRange(filter, partitionRange);
            }
        }

        return partitionRanges.stream().map(this::parseFilter).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private void verifyPartitionRange(Filter filter, Filter partitionRange) {

        if(queryContext.getQueryParam().getType() == QueryTypeEnum.TOTAL) {
            return;
        }

        if(!filter.getName().equals(partitionRange.getName())) {
            return;
        }
        if(!filter.getDataType().isTime()) {
            return;
        }
        if(!partitionRange.getDataType().isTime()){
            return;
        }
        List<Condition> filterConditions = filter.getConditions();
        if(Objects.isNull(filterConditions) || filterConditions.isEmpty()) {
            return;
        }
        List<LocalDateTime> filterDateTimes = parseTimeRange(filterConditions.get(0));

        List<Condition> partitionConditions = partitionRange.getConditions();
        if(Objects.isNull(partitionConditions) || partitionConditions.isEmpty()){
            return;
        }
        if(Objects.isNull(partitionConditions.get(0).getArgs())){
            return;
        }
        List<LocalDateTime> partitionDateTimes = parseTimeRange(partitionConditions.get(0));

        if(partitionDateTimes.size() == 1) {
            LocalDateTime partitionDateTime = partitionDateTimes.get(0);
            if(filterDateTimes.size() == 1) {
                LocalDateTime filterDateTime = filterDateTimes.get(0);
                if (!partitionDateTime.isEqual(filterDateTime)) {
                    throw new ServiceException(LocaleMessages.getMessage(MessageConsist.PARTITION_OUT_OF_RANGE));
                }
            } else {
                LocalDateTime start = filterDateTimes.get(0);
                LocalDateTime end = filterDateTimes.get(1);

                if(start.isAfter(partitionDateTime) || end.isBefore(partitionDateTime)) {
                    throw new ServiceException(LocaleMessages.getMessage(MessageConsist.PARTITION_OUT_OF_RANGE));
                }
            }
        } else {
            LocalDateTime partitionStart = partitionDateTimes.get(0);
            LocalDateTime partitionEnd = partitionDateTimes.get(1);

            if(filterDateTimes.size() == 1) {
                LocalDateTime filterDateTime = filterDateTimes.get(0);
                if (filterDateTime.isAfter(partitionEnd) || filterDateTime.isBefore(partitionStart)) {
                    throw new ServiceException(LocaleMessages.getMessage(MessageConsist.PARTITION_OUT_OF_RANGE));
                }
            } else {
                LocalDateTime start = filterDateTimes.get(0);
                LocalDateTime end = filterDateTimes.get(1);

                if(start.isAfter(partitionEnd) || end.isBefore(partitionStart)) {
                    throw new ServiceException(LocaleMessages.getMessage(MessageConsist.PARTITION_OUT_OF_RANGE));
                }
            }
        }
    }

    private List<Segment> parseRowPrivilege(Set<String> rowPrivileges) {

        if (rowPrivileges == null || rowPrivileges.isEmpty()) {
            return Lists.newArrayList();
        }

        return rowPrivileges.stream().map(row -> {
            String expression = expressionParser.buildExpression(row);
            return new Segment(expression);
        }).collect(Collectors.toList());

    }

    public List<Segment> parseDimension(Dimension dimension) {

        String name = dimension.getName();
        DatasetFieldDTO field = queryContext.getFieldMap().get(name);

        String expression = expressionParser.parse(field);

        if (Strings.isNullOrEmpty(expression)) {
            return Lists.newArrayList();
        }

        if (!field.getDataType().isTime()) {
            return Lists.newArrayList(new Segment(name, expression));
        }

        if (Strings.isNullOrEmpty(dimension.getDateTrunc())) {
            return Lists.newArrayList(new Segment(name, expression));
        }

        // 日期分组
        List<Segment> segments = Lists.newArrayList();
        DateTruncEnum dataTrunc = DateTruncEnum.valueOf(dimension.getDateTrunc());
        switch (dataTrunc) {
            case YEAR:
                segments.add(new Segment(name, typeConverter.toStartOfYear(expression)));
                break;
            case QUARTER:
                segments.add(new Segment(name, typeConverter.toStartOfQuarter(expression)));
                break;
            case MONTH:
                segments.add(new Segment(name, typeConverter.toStartOfMonth(expression)));
                break;
            case WEEK:
                segments.add(new Segment(name, typeConverter.toStartOfWeek(expression, dimension.getFirstDayOfWeek())));
                break;
            case DAY:
                segments.add(new Segment(name, typeConverter.toDay(expression)));
                break;
            case HOUR:
                segments.add(new Segment(name, typeConverter.toStartOfHour(expression)));
                break;
            case MINUTE_30:
            case MINUTE_20:
            case MINUTE_15:
            case MINUTE_10:
            case MINUTE_5:
                segments.add(new Segment(name, typeConverter.toStartOfMinute(expression, dataTrunc.getWindowMinute())));
                break;
            default:
//                segments.add(new Segment(name, typeConverter.toDay(expression)));
                segments.add(new Segment(name,expression));
                break;
        }

        return segments;
    }

    public Segment parseMeasure(Measure measure) {

        DatasetFieldDTO field = queryContext.getFieldMap().get(measure.getName());

        String expression = expressionParser.parse(field);
        if (Strings.isNullOrEmpty(expression)) {
            return null;
        }

        String aggregation = Aggregators.of(measure.getAggregator()).buildAggregationExpression(expression);

        if (!Strings.isNullOrEmpty(field.getComputeExpression())) {
            String computed = String.format("( (%s) %s)", aggregation, field.getComputeExpression());
            return new Segment(measure.getName(), measure.getAggregator(), computed);
        } else {
            return new Segment(measure.getName(), measure.getAggregator(), aggregation);
        }
    }

    public Segment parseFilter(Filter filter) {

        DatasetFieldDTO field = queryContext.getFieldMap().get(filter.getName());

        String expression = expressionParser.parse(field);
        if (Strings.isNullOrEmpty(expression)) {
            return null;
        }

        List<String> conditionExpression = filter.getConditions().stream()
                .map(condition -> parseCondition(condition, expression, field))
                .filter(item -> !Strings.isNullOrEmpty(item))
                .collect(Collectors.toList());

        if (conditionExpression.isEmpty()) {
            return null;
        }
        LogicalEnum logical = Objects.nonNull(filter.getLogical()) ? filter.getLogical() : LogicalEnum.AND;
        return new Segment(filter.getName(), String.format("(%s)", Joiner.on(logical.getExpression()).join(conditionExpression)));

    }

    public Segment parseHaving(Filter filter, List<Segment> measureSegments) {

        DatasetFieldDTO field = queryContext.getFieldMap().get(filter.getName());

        String expression = measureSegments.stream().filter(segment -> filter.getName().equals(segment.getName())).findFirst().get().getExpression();

        List<String> conditionExpression = filter.getConditions().stream().map(condition -> parseCondition(condition, expression, field)).filter(item -> !Strings.isNullOrEmpty(item)).collect(Collectors.toList());

        if (conditionExpression.isEmpty()) {
            return null;
        }
        return new Segment(filter.getName(), String.format("(%s)", Joiner.on(filter.getLogical().getExpression()).join(conditionExpression)));

    }

    public String parseCondition(Condition condition, String expression, DatasetFieldDTO field) {

        if (condition.argIsEmpty() && !condition.isUseLatestPartitionValue()) {
            return StringUtils.EMPTY;
        }

        OperatorParam param = new OperatorParam(condition.getArgs(), expression, field);

        // time
        if (field.getDataType().isTime()) {
            List<String> args = parseTimeRange(condition).stream()
                    .map(time -> field.getDataType() == DataTypeEnum.TIME_YYYYMMDD_HHMMSS ? time.format(BiConsist.YYYY_MM_DD_HH_MM_SS_FORMATTER) : time.format(BiConsist.YYYY_MM_DD_FORMATTER))
                    .collect(Collectors.toList());
            param.setArgs(args);

            return FunctionalOperatorEnum.BETWEEN.getOperator().apply(param);
        }

        // text, number
        if (typeConverter.isDecimal(field.getDatabaseDataType())) {
            List<String> args = condition.getArgs().stream().map(arg -> typeConverter.toDecimal(arg)).collect(Collectors.toList());
            param.setArgs(args);
        }

        return condition.getFunctionalOperator().getOperator().apply(param);
    }

    public List<LocalDateTime> parseTimeRange(Condition condition) {

        if (condition.isUseLatestPartitionValue()) {
            return TimeTypeEnum.EXACT.getDateTime(Lists.newArrayList(getDataset().getLatestPartitionValue(), getDataset().getLatestPartitionValue()), Lists.newArrayList());
        }

        List<String> args = condition.getArgs();

        if (args.size() == 1) {
            args = Lists.newArrayList(args.get(0), args.get(0));
        }

        return condition.getTimeType().getDateTime(args, condition.getTimeParts());

    }

    public Segment parseSortBy(Sort sort, List<Segment> measureSegments) {

        String fieldName = sort.getSortField();
        Segment measure = measureSegments.stream().filter(segment -> fieldName.equals(segment.getName()) || fieldName.equals(String.format("%s.%s", segment.getName(), segment.getAggregator()))).findFirst().get();

        if (Objects.isNull(sort.getLimit())) {
            return new Segment(fieldName, String.format(" %s %s ", measure.getExpression(), sort.getSortType()));
        } else {
            return new Segment(fieldName, String.format(" %s %s limit %d", measure.getExpression(), sort.getSortType(), sort.getLimit()));
        }
    }

    public Segment parsePaging(Paging paging) {
        if (paging == null || !paging.hasLimit()) {
            paging = Paging.getDefault();
        }

        Long limit = Math.min(paging.getLimit(), queryContext.isDownload() ? BiConsist.MAX_DOWNLOAD_NUM : BiConsist.MAX_QUERY_NUM);
        if (paging.hasOffset()) {
            return new Segment(String.format(" %d , %d", paging.getOffset(), limit));
        }
        return new Segment(limit.toString());
    }
    private List<RatioPart> parseRatioPart(Compare compare, QueryParam queryParam, List<Segment> dimensionSegments) {

        String timeField = compare.getTimeField();

        Optional<Dimension> timeDimension = queryParam.getDimensions().stream().filter(dim -> dim.getName().equals(timeField)).findFirst();
        DateTruncEnum dateTrunc = Strings.isNullOrEmpty(timeDimension.get().getDateTrunc()) ? DateTruncEnum.ORIGIN : DateTruncEnum.valueOf(timeDimension.get().getDateTrunc());

        Segment timeDimSegment = dimensionSegments.stream().filter(dim -> dim.getName().equals(timeField)).findFirst().get();

        List<RatioMeasure> measures = compare.getMeasures();
        // compatible old version
        for(RatioMeasure ratioMeasure : measures) {
            if(Objects.isNull(ratioMeasure.getRatioType())) {
                ratioMeasure.setRatioType(compare.getType());
            }
            if(Objects.isNull(ratioMeasure.getPeriod())) {
                if ((dateTrunc == DateTruncEnum.DAY || dateTrunc == DateTruncEnum.ORIGIN) && ratioMeasure.getRatioType() != RatioTypeEnum.CHAIN) {
                    ratioMeasure.setPeriod(RatioPeriodEnum.WHOLE);
                } else {
                    ratioMeasure.setPeriod(RatioPeriodEnum.SAME);
                }
            }
        }

        Map<RatioMeasure.RatioPair, List<RatioMeasure>> groupingMeasures = measures.stream().collect(Collectors.groupingBy(RatioMeasure::getRatioPari));

        return groupingMeasures.entrySet().stream().map( entry -> {
            RatioMeasure.RatioPair ratioPair = entry.getKey();
            Segment joinOnField = parseJoinOnField(ratioPair.getType(), dateTrunc, timeDimSegment);

            List<Segment> ratioTimeRanges = parseRatioTimeRange(compare, queryParam, ratioPair.getType(), ratioPair.getPeriod());

            return  RatioPart.builder()
                    .type(ratioPair.getType())
                    .period(ratioPair.getPeriod())
                    .ratioMeasures(entry.getValue())
                    .joinOnSegment(joinOnField)
                    .timeRanges(ratioTimeRanges)
                    .build();
            }
        ).collect(Collectors.toList());
    }

    public List<Segment> parseRatioTimeRange(Compare compare, QueryParam queryParam, RatioTypeEnum ratioType, RatioPeriodEnum ratioPeriod) {

        String timeFieldName = compare.getTimeField();
        DatasetFieldDTO timeField = queryContext.getFieldMap().get(timeFieldName);

        List<Filter> filterFields = queryParam.getFilters().stream().filter(filter -> filter.getName().equals(timeFieldName) || BiConsist.DEFAULT_PARTITION_NAME.equals(filter.getName())).collect(Collectors.toList());
        Optional<Filter> partitionFilterFieldOptional = queryParam.getFilters().stream().filter(filter -> BiConsist.DEFAULT_PARTITION_NAME.equals(filter.getName())).findFirst();
        Optional<Filter> timeFilterFieldOptional = queryParam.getFilters().stream().filter(filter -> filter.getName().equals(timeFieldName)).findFirst();

        // same period and ratio time field is time yyyymmdd_hhMMss
        if (!timeFilterFieldOptional.isPresent()
                && partitionFilterFieldOptional.isPresent()
                && timeField.getDataType() == DataTypeEnum.TIME_YYYYMMDD_HHMMSS
                && ratioPeriod == RatioPeriodEnum.SAME) {
            Condition partitionCondition = partitionFilterFieldOptional.get().getConditions().get(0);
            Filter additionFilter = new Filter();
            Condition condition = new Condition();
            condition.setTimeType(partitionCondition.getTimeType())
                    .setArgs(partitionCondition.getArgs())
                    .setTimeParts(Lists.newArrayList(BiConsist.TIME_START_OF_DAY, LocalTime.now().format(BiConsist.HH_MM_SS_FORMATTER)));

            additionFilter.setName(timeFieldName)
                    .setLogical(LogicalEnum.AND)
                    .setDataType(timeField.getDataType())
                    .setConditions(Lists.newArrayList(condition));

            filterFields.add(additionFilter);
        }

        return filterFields.stream().map(filterField -> {
            List<LocalDateTime> timeRange = filterField.getConditions().stream()
                    .flatMap(condition -> parseTimeRange(condition).stream())
                    .collect(Collectors.toList());

            String filterFieldName = filterField.getName();

            DateTruncEnum dataTrunc = DateTruncEnum.ORIGIN;
            Optional<Dimension> dimField = queryParam.getDimensions().stream().filter(dimension -> dimension.getName().equals(timeFieldName)).findFirst();
            if (dimField.isPresent()) {
                String dataTruncStr = dimField.get().getDateTrunc();
                if (!Strings.isNullOrEmpty(dataTruncStr)) {
                    dataTrunc = DateTruncEnum.valueOf(dataTruncStr);
                }
            }
            DatasetFieldDTO field = queryContext.getFieldMap().get(filterFieldName);
            List<String> rationTimeRange = ratioType.transformTime(timeRange, dataTrunc, ratioPeriod, timeField)
                    .stream()
                    .map(time -> field.getDataType() == DataTypeEnum.TIME_YYYYMMDD_HHMMSS ?  time.format(BiConsist.YYYY_MM_DD_HH_MM_SS_FORMATTER) : time.format(BiConsist.YYYY_MM_DD_FORMATTER))
                    .collect(Collectors.toList());

            String expression = expressionParser.parse(field);

            OperatorParam param = new OperatorParam(rationTimeRange, expression, field);

            if (rationTimeRange.size() == 1) {
                return new Segment(filterFieldName, FunctionalOperatorEnum.EQUAL.getOperator().apply(param));
            } else {
                return new Segment(filterFieldName, FunctionalOperatorEnum.BETWEEN.getOperator().apply(param));
            }
        }).collect(Collectors.toList());
    }


    public Segment parseJoinOnField(RatioTypeEnum ratioType, DateTruncEnum dateTrunc, Segment timeSegment) {

        if(Objects.isNull(timeSegment)) {
            return null;
        }
        DatasetFieldDTO field = queryContext.getFieldMap().get(timeSegment.getName());
        String expression = ratioType.transformDimension(timeSegment.getExpression(), dateTrunc, typeConverter, field);
        return new Segment(timeSegment.getName(), expression);
    }

    private boolean isHavingFilter(Filter filter) {
        DatasetFieldDTO field = queryContext.getFieldMap().get(filter.getName());
        String expression = expressionParser.parse(field);
        return filter.isHaving() || FieldUtils.hasAggregation(expression);
    }

    private DatasetFieldDTO getPartitionField() {
        try {

            Optional<DatasetFieldDTO> field = getDataset().getFields()
                    .stream()
                    .filter(f -> Objects.nonNull(f.getPartition()) && f.getPartition() == 1)
                    .filter(Objects::nonNull)
                    .findFirst();

            if (field.isPresent()) {
                return field.get();
            }

            return getDataset().getFields().stream().filter(f -> f.getName().equals(BiConsist.DEFAULT_PARTITION_NAME)).findFirst().get();
        } catch (Exception e) {
            log.error("get partition field error", e);
        }
        return null;
    }

    public DatasetDTO getDataset() {
        return dataset;
    }


    private String getTable() {
        if(queryContext.getConnectionParam().isRealtime()) {
            return tableStage.getRealTimeTable();
        } else {
            return tableStage.getTable();
        }
    }
}
