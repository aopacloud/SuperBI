package net.aopacloud.superbi.queryEngine.model;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.aopacloud.superbi.common.core.context.LoginContextHolder;
import net.aopacloud.superbi.common.core.utils.bean.BeanUtils;
import net.aopacloud.superbi.enums.EngineEnum;
import net.aopacloud.superbi.enums.FieldCategoryEnum;
import net.aopacloud.superbi.model.dto.ConnectionParamDTO;
import net.aopacloud.superbi.model.dto.DatasetDTO;
import net.aopacloud.superbi.model.dto.DatasetFieldDTO;
import net.aopacloud.superbi.model.dto.TablePartition;
import net.aopacloud.superbi.queryEngine.enums.DateTruncEnum;
import net.aopacloud.superbi.queryEngine.util.RatioLabelGenerator;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: hudong
 * @date: 2023/8/15
 * @description:
 */
@Data
@NoArgsConstructor
public class QueryContext {

    private QueryParam queryParam;

    private DatasetDTO dataset;

    /**
     * field name -> field
     */
    private Map<String, DatasetFieldDTO> fieldMap;

    private ConnectionParamDTO connectionParam;

    private String username = LoginContextHolder.getUsername();

    private String token = LoginContextHolder.getToken();

    /**
     * execute sql
     */
    private String sql;

    /**
     * user row privileges if exists
     */
    private Set<String> rowPrivileges;

    private boolean isDownload = Boolean.FALSE;

    private List<TablePartition> latestPartitions;

    private QueryContext(QueryParam queryParam, DatasetDTO dataset, ConnectionParamDTO connectionParam) {
        this.queryParam = queryParam;
        this.dataset = dataset;
        if (Objects.nonNull(dataset) && Objects.nonNull(dataset.getFields())) {
            fieldMap = dataset.getFields().stream().collect(Collectors.toMap(DatasetFieldDTO::getName, field -> field));
        }
        this.connectionParam = connectionParam;
    }

    public static QueryContext ofQuery(QueryParam queryParam, DatasetDTO dataset, ConnectionParamDTO connection) {
        return new QueryContext(queryParam, dataset, connection);
    }

    public static QueryContext ofDownload(QueryParam queryParam, DatasetDTO dataset, ConnectionParamDTO connection) {
        QueryContext queryContext = new QueryContext(queryParam, dataset, connection);
        queryContext.setDownload(Boolean.TRUE);
        return queryContext;
    }

    public QueryContext clone() {
        QueryContext clone = new QueryContext();
        BeanUtils.copyBeanProp(clone, this);
        return clone;
    }

    public String getTable() {
        return String.format("%s.%s", dataset.getConfig().getDbName(), dataset.getConfig().getTableName());
    }

    public EngineEnum getEngine() {
        return connectionParam.getEngine();
    }
    public List<String> getTitles() {
        return getColumns().stream().map(QueryColumn::getDisplayName).collect(Collectors.toList());
    }
    public List<QueryColumn> getColumns() {

        List<QueryColumn> columns = Lists.newArrayList();
        QueryParam queryParam = getQueryParam();
        Map<String, DatasetFieldDTO> fieldMap = getFieldMap();

        for (Dimension dim : queryParam.getDimensions()) {

            DatasetFieldDTO datasetFieldDTO = fieldMap.get(dim.getName());
            QueryColumn column = QueryColumn.of(datasetFieldDTO);
            column.setCategory(FieldCategoryEnum.DIMENSION);

            if (!Strings.isNullOrEmpty(dim.getDisplayName())) {
                column.setDisplayName(dim.getDisplayName());
            }
            columns.add(column);
        }

        List<RatioMeasure> ratioMeasures = Lists.newArrayList();
        Compare compare = queryParam.getCompare();
        DateTruncEnum dateTrunc = DateTruncEnum.ORIGIN;
        if (Objects.nonNull(compare)) {
            ratioMeasures = compare.getMeasures();
            Optional<Dimension> timeField = queryParam.getDimensions().stream().filter(dim -> dim.getName().equals(compare.getTimeField())).findFirst();
            if (timeField.isPresent()) {
                String dateTruncStr = timeField.get().getDateTrunc();
                if (!Strings.isNullOrEmpty(dateTruncStr)) {
                    dateTrunc = DateTruncEnum.valueOf(dateTruncStr);
                }
            }
        }

        for (Measure measure : queryParam.getMeasures()) {

            DatasetFieldDTO datasetFieldDTO = fieldMap.get(measure.getName());
            QueryColumn column = QueryColumn.of(datasetFieldDTO);
            column.setAggregator(measure.getAggregator());
            column.setCategory(FieldCategoryEnum.MEASURE);
            columns.add(column);

            String displayName = Strings.isNullOrEmpty(measure.getDisplayName()) ? datasetFieldDTO.getDisplayName() : measure.getDisplayName();
            column.setDisplayName(displayName);

            for (RatioMeasure ratioMeasure : ratioMeasures) {
                if (ratioMeasure.getName().equals(measure.getName())) {
                    QueryColumn ratioColumn = column.copy();
                    ratioColumn.setDisplayName(String.format("%s-%s", displayName, RatioLabelGenerator.getLabel(dateTrunc, ratioMeasure.getRatioType(), ratioMeasure.getPeriod())));
                    columns.add(ratioColumn);
                }
            }

        }
        return columns;
    }
}
