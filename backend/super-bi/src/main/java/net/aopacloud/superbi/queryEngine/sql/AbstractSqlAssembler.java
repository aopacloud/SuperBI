package net.aopacloud.superbi.queryEngine.sql;

import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import net.aopacloud.superbi.common.core.utils.StringUtils;
import net.aopacloud.superbi.constant.BiConsist;
import net.aopacloud.superbi.enums.DataTypeEnum;
import net.aopacloud.superbi.enums.FieldCategoryEnum;
import net.aopacloud.superbi.enums.FieldTypeEnum;
import net.aopacloud.superbi.model.dto.DatasetDTO;
import net.aopacloud.superbi.model.dto.DatasetFieldDTO;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import net.aopacloud.superbi.model.dto.TablePartition;
import net.aopacloud.superbi.queryEngine.enums.DateTruncEnum;
import net.aopacloud.superbi.queryEngine.enums.TimeTypeEnum;
import net.aopacloud.superbi.queryEngine.model.*;
import net.aopacloud.superbi.queryEngine.sql.analytic.*;
import net.aopacloud.superbi.queryEngine.sql.operator.FunctionalOperatorEnum;
import net.aopacloud.superbi.queryEngine.sql.operator.OperatorParam;
import net.aopacloud.superbi.util.FieldUtils;

import java.time.LocalDateTime;
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

    protected Map<String, DatasetFieldDTO> fieldMap;

    public AbstractSqlAssembler(DatasetDTO dataset, TypeConverter typeConverter,QueryContext queryContext) {
        this.dataset = dataset;
        this.typeConverter = typeConverter;
        this.queryContext = queryContext;
        fieldMap = dataset.getFields().stream().collect(Collectors.toMap(DatasetFieldDTO::getName, field -> field));
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

        switch (queryParam.getType()) {
            case QUERY:
                return new QueryAnalysisModel().addDimensions(dimensions).addMeasure(measures)
                        .addWhere(filters)
                        .addHaving(having)
                        .addGroupBy(dimensions)
                        .setTable(queryContext.getTable())
                        .setOrderBy(orderBy)
                        .setPaging(paging);

            case TOTAL:
                return new TotalAnalysisModel().setTable(queryContext.getTable())
                        .setGroupBy(dimensions)
                        .setWhere(filters)
                        .setHaving(having);

            case SINGLE_FIELD:
                SingleFieldAnalysisModel model = new SingleFieldAnalysisModel()
                        .setTable(queryContext.getTable())
                        .setField(dimensions.get(0))
                        .setPaging(paging);

                List<TablePartition> partitions = queryContext.getLatestPartitions();
                if(!Objects.isNull(partitions) && !partitions.isEmpty()) {
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
                Segment joinOnField = parseJoinOnField(compare,dimensions);
                Segment ratioTimeRange = parseRatioTimeRange(compare, queryParam);

                return new RatioQueryAnalysisModel().setDimension(dimensions)
                        .setMeasures(measures)
                        .setWhere(filters)
                        .setHaving(having)
                        .setGroupBy(dimensions)
                        .setTable(queryContext.getTable())
                        .setOrderBy(orderBy)
                        .setPaging(paging)
                        .setJoinOnField(joinOnField)
                        .setRatioTimeRange(ratioTimeRange)
                        .setRatioMeasures(compare.getMeasures());

            case PREVIEW:
                List<Segment> dimSegments = queryParam.getDataset().getFields().stream()
                        .filter(field -> field.getCategory() == FieldCategoryEnum.DIMENSION)
                        .map(field -> new Segment(field.getName(), getFieldExpression(field)))
                        .collect(Collectors.toList());

                List<Segment> measureSegments = queryParam.getDataset().getFields().stream()
                        .filter(field -> field.getCategory() == FieldCategoryEnum.MEASURE)
                        .map(field -> new Segment(field.getName(), field.getAggregator() ,field.getAggregator().buildAggregationExpression(getFieldExpression(field))))
                        .collect(Collectors.toList());

                DatasetPreviewAnalysisModel previewAnalysisModel = new DatasetPreviewAnalysisModel()
                        .setTable(queryContext.getTable())
                        .setDimensions(dimSegments)
                        .setMeasures(measureSegments);

                List<TablePartition> latestPartitions = queryContext.getLatestPartitions();

                if(!Objects.isNull(latestPartitions) && !latestPartitions.isEmpty()) {
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

    private List<Segment> parseRowPrivilege(Set<String> rowPrivileges) {

        if(rowPrivileges == null || rowPrivileges.isEmpty()) {
            return Lists.newArrayList();
        }

        return rowPrivileges.stream().map(row -> {
            String expression = buildExpression(row);
            return new Segment(expression);
        }).collect(Collectors.toList());

    }

    public List<Segment> parseDimension(Dimension dimension) {

        String name = dimension.getName();
        DatasetFieldDTO field = queryContext.getFieldMap().get(name);

        String expression = getFieldExpression(field);

        if(org.assertj.core.util.Strings.isNullOrEmpty(expression)) {
            return Lists.newArrayList();
        }

        if (!field.getDataType().isTime()) {
            return Lists.newArrayList(new Segment(name, expression));
        }

        if (org.assertj.core.util.Strings.isNullOrEmpty(dimension.getDateTrunc())) {
            return Lists.newArrayList(new Segment(name, expression));
        }

        // 日期分组
        List<Segment> segments = Lists.newArrayList();
        DateTruncEnum dataTrunc = DateTruncEnum.valueOf(dimension.getDateTrunc());
        switch (dataTrunc) {
            case YEAR:
                segments.add(new Segment(name, typeConverter.toStartOfYear(expression)));
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
            default:
                segments.add(new Segment(name, typeConverter.toDay(expression)));
                break;
        }

        return segments;
    }

    public Segment parseMeasure(Measure measure) {

        DatasetFieldDTO field = queryContext.getFieldMap().get(measure.getName());

        String expression = getFieldExpression(field);
        if(org.assertj.core.util.Strings.isNullOrEmpty(expression)) {
            return null;
        }

        String aggregation = measure.getAggregator().buildAggregationExpression(expression);

        if(!org.assertj.core.util.Strings.isNullOrEmpty(field.getComputeExpression())) {
            String computed = String.format("( (%s) %s)", aggregation, field.getComputeExpression());
            return new Segment(measure.getName(), measure.getAggregator(), computed);
        } else {
            return new Segment(measure.getName(), measure.getAggregator(), aggregation);
        }
    }

    public Segment parseFilter(Filter filter) {

        DatasetFieldDTO field = queryContext.getFieldMap().get(filter.getName());

        String expression = getFieldExpression(field);
        if(org.assertj.core.util.Strings.isNullOrEmpty(expression)) {
            return null;
        }

        List<String> conditionExpression = filter.getConditions().stream()
                .map(condition -> parseCondition(condition, expression, field))
                .filter(item -> !org.assertj.core.util.Strings.isNullOrEmpty(item))
                .collect(Collectors.toList());

        if(conditionExpression.isEmpty()) {
            return null;
        }

        return new Segment(filter.getName(), String.format("(%s)", Joiner.on(filter.getLogical().getExpression()).join(conditionExpression)));

    }

    public Segment parseHaving(Filter filter, List<Segment> measureSegments) {

        DatasetFieldDTO field = queryContext.getFieldMap().get(filter.getName());

        String expression = measureSegments.stream().filter(segment -> filter.getName().equals(segment.getName())).findFirst().get().getExpression();

        List<String> conditionExpression = filter.getConditions().stream().map(condition -> parseCondition(condition, expression, field)).filter(item -> !org.assertj.core.util.Strings.isNullOrEmpty(item)).collect(Collectors.toList());

        if(conditionExpression.isEmpty()) {
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
                    .map(time -> field.getDataType() == DataTypeEnum.TIME_YYYYMMDD_HHMMSS ?  time.format(BiConsist.YYYY_MM_DD_HH_MM_SS_FORMATTER) : time.format(BiConsist.YYYY_MM_DD_FORMATTER))
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
            return TimeTypeEnum.EXACT.getDateTime(Lists.newArrayList(getDataset().getLatestPartitionValue(), getDataset().getLatestPartitionValue()));
        }

        List<String> args = condition.getArgs();

        if(args.size() == 1) {
            args = Lists.newArrayList(args.get(0), args.get(0));
        }

        return condition.getTimeType().getDateTime(args);

    }

    public Segment parseSortBy(Sort sort, List<Segment> measureSegments) {

        String fieldName = sort.getSortField();
        Segment measure = measureSegments.stream().filter(segment -> fieldName.equals(segment.getName()) || fieldName.equals(String.format("%s.%s", segment.getName(), segment.getAggregator().name()))).findFirst().get();

        if(Objects.isNull(sort.getLimit())) {
            return new Segment(fieldName, String.format(" %s %s ", measure.getExpression(), sort.getSortType()));
        } else {
            return new Segment(fieldName, String.format(" %s %s limit %d", measure.getExpression(), sort.getSortType(), sort.getLimit()));
        }
    }

    public Segment parsePaging(Paging paging) {
        if (paging == null || !paging.hasLimit()) {
            paging = Paging.getDefault();
        }

        Long limit = Math.min(paging.getLimit(), queryContext.isDownload()? BiConsist.MAX_DOWNLOAD_NUM : BiConsist.MAX_QUERY_NUM);
        if (paging.hasOffset()) {
            return new Segment(String.format(" %d , %d", paging.getOffset(), limit));
        }
        return new Segment(limit.toString());
    }

    public Segment parseRatioTimeRange(Compare compare, QueryParam queryParam) {

        String timeField = compare.getTimeField();
        Optional<Filter> filterOptional = queryParam.getFilters().stream().filter(filter -> filter.getName().equals(timeField)).findFirst();
        if (filterOptional.isPresent()) {

            List<LocalDateTime> timeRange = filterOptional.get().getConditions().stream()
                    .flatMap(condition -> parseTimeRange(condition).stream())
                    .collect(Collectors.toList());

            DateTruncEnum dataTrunc = DateTruncEnum.DAY;
            Optional<Dimension> dimField = queryParam.getDimensions().stream().filter(dimension -> dimension.getName().equals(timeField)).findFirst();
            if (dimField.isPresent()) {
                String dataTruncStr = dimField.get().getDateTrunc();
                if (!org.assertj.core.util.Strings.isNullOrEmpty(dataTruncStr)) {
                    dataTrunc = DateTruncEnum.valueOf(dataTruncStr);
                }
            }
            DatasetFieldDTO field = queryContext.getFieldMap().get(timeField);
            List<String> rationTimeRange = compare.getType().transformTime(timeRange, dataTrunc)
                    .stream()
                    .map(time -> field.getDataType() == DataTypeEnum.TIME_YYYYMMDD_HHMMSS ?  time.format(BiConsist.YYYY_MM_DD_HH_MM_SS_FORMATTER) : time.format(BiConsist.YYYY_MM_DD_FORMATTER))
                    .collect(Collectors.toList());
            String expression = getFieldExpression(queryContext.getFieldMap().get(timeField));
            OperatorParam param = new OperatorParam(rationTimeRange, expression, field);

            if (rationTimeRange.size() == 1) {
                return new Segment(timeField, FunctionalOperatorEnum.EQUAL.getOperator().apply(param));
            } else {
                return new Segment(timeField, FunctionalOperatorEnum.BETWEEN.getOperator().apply(param));
            }
        }
        return null;
    }

    public Segment parseJoinOnField(Compare compare, List<Segment> dimensions) {
        String name = compare.getTimeField();
        Optional<Segment> dimSegment = dimensions.stream().filter(dim -> dim.getName().equals(name)).findFirst();
        if (!dimSegment.isPresent()) {
            return null;
        }
        Optional<Dimension> dimParam = queryContext.getQueryParam().getDimensions().stream().filter(dim -> dim.getName().equals(name)).findFirst();
        if(!dimParam.isPresent()){
            return null;
        }
        DateTruncEnum dateTrunc = org.assertj.core.util.Strings.isNullOrEmpty(dimParam.get().getDateTrunc()) ? DateTruncEnum.ORIGIN : DateTruncEnum.valueOf(dimParam.get().getDateTrunc());

        String expression = compare.getType().transformDimension(dimSegment.get().getExpression(), dateTrunc, typeConverter);
        return new Segment(name, expression);
    }

    private boolean isHavingFilter(Filter filter) {
        DatasetFieldDTO field = queryContext.getFieldMap().get(filter.getName());
        String expression = getFieldExpression(field);
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
        }catch (Exception e) {
            log.error("get partition field error", e);
        }
        return null;
    }

    /**
     * get field expression.
     * if field type is origin, return field name.
     * else return field expression than parsed with variable.
     * @param field
     * @return
     */
    protected String getFieldExpression(DatasetFieldDTO field) {
        if (Objects.isNull(field)) {
            return StringUtils.EMPTY;
        }
        if (field.getType() == FieldTypeEnum.ORIGIN) {
            return switchDateType(String.format("`%s`",field.getName()), field);
        }
        String expression = field.getExpression();

        if (Strings.isNullOrEmpty(expression)) {
            return StringUtils.EMPTY;
        }

        return buildExpression(expression);
    }


    /**
     * recursion build expression until expression not contains '['
     * @param expression
     * @return
     */
    protected String buildExpression(String expression) {
        if (!Strings.isNullOrEmpty(expression) && expression.contains("[") && expression.contains("]")) {
            List<String> fieldNames = extractFieldNames(expression);
            for (String fieldName : fieldNames) {
                DatasetFieldDTO replaceField = fieldMap.get(fieldName);
                if (replaceField != null) {
                    String replaceExpression = replaceField.getType() == FieldTypeEnum.ORIGIN ? switchDateType(replaceField.getName(), replaceField) : replaceField.getExpression();
                    String newExpression = expression.replaceAll(String.format("\\[%s\\]", fieldName), "(" + replaceExpression + ")");
                    return buildExpression(newExpression);
                }
            }
            return expression;
        }
        return expression;
    }

    private List<String> extractFieldNames(String expression) {
        List<String> list = Lists.newArrayList();
        int start = 0;
        int startFlag = 0;
        int endFlag = 0;
        for (int i = 0; i < expression.length(); i++) {
            if (expression.charAt(i) == '[') {
                startFlag++;
                if (startFlag == endFlag + 1) {
                    start = i;
                }
            } else if (expression.charAt(i) == ']') {
                endFlag++;
                if (endFlag == startFlag) {
                    list.add(expression.substring(start + 1, i));
                }
            }
        }
        return list;
    }

    private String switchDateType(String expression, DatasetFieldDTO field) {

        if(field.getType().isNewAdd()) {
            return expression;
        }
        if(Objects.isNull(field.getOriginDataType())) {
            return expression;
        }
        if(field.getDataType() == field.getOriginDataType()) {
            return expression;
        }

        if(field.getDataType().isText()) {
            return typeConverter.toString(expression);
        }

        // to number
        if(field.getDataType() == DataTypeEnum.NUMBER){
            // string to number
            if(field.getOriginDataType().isText()){
                return typeConverter.stringToNumber(expression);
            }
            // time to number
            if(field.getOriginDataType().isTime()){
                return typeConverter.timeToNumber(expression);
            }
        }
        // to time
        if(field.getDataType().isTime()){
            // string to time
            if(field.getOriginDataType().isText()){
                String convertExpression = typeConverter.stringToDate(expression);
                if(field.getDataType() == DataTypeEnum.TIME_YYYYMMDD_HHMMSS){
                    convertExpression = typeConverter.stringToDateTime(expression);
                }
                return convertExpression;
            }

            // 数字转成时间
            if(field.getOriginDataType() == DataTypeEnum.NUMBER){
                return typeConverter.numberToDate(expression);
            }
        }
        return expression;
    }

    public DatasetDTO getDataset() {
        return dataset;
    }
}
