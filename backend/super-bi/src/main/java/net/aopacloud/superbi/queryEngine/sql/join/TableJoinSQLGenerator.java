package net.aopacloud.superbi.queryEngine.sql.join;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import net.aopacloud.superbi.constant.BiConsist;
import net.aopacloud.superbi.enums.FieldCategoryEnum;
import net.aopacloud.superbi.model.domain.MetaConfigContent;
import net.aopacloud.superbi.model.domain.TableDescriptor;
import net.aopacloud.superbi.model.domain.TableJoinDescriptor;
import net.aopacloud.superbi.model.dto.DatasetDTO;
import net.aopacloud.superbi.model.dto.DatasetFieldDTO;
import net.aopacloud.superbi.queryEngine.model.Filter;
import net.aopacloud.superbi.queryEngine.model.Measure;
import net.aopacloud.superbi.queryEngine.model.QueryContext;
import net.aopacloud.superbi.queryEngine.model.QueryParam;
import net.aopacloud.superbi.queryEngine.sql.AbstractSqlAssembler;
import net.aopacloud.superbi.queryEngine.sql.Segment;
import net.aopacloud.superbi.util.Mocks;
import org.apache.commons.compress.utils.Lists;
import org.assertj.core.util.Sets;

import java.util.*;
import java.util.stream.Collectors;

public class TableJoinSQLGenerator {

    private MetaConfigContent metaConfigContent;

    private DatasetDTO datasetDTO;

    private AbstractSqlAssembler sqlAssembler;

    private List<Segment> pushDownFilters;

    public TableJoinSQLGenerator(MetaConfigContent metaConfigContent, DatasetDTO datasetDTO, AbstractSqlAssembler sqlAssembler) {
        this.metaConfigContent = metaConfigContent;
        this.datasetDTO = datasetDTO;
        this.sqlAssembler = sqlAssembler;
    }

    public TableJoinSQLGenerator(MetaConfigContent metaConfigContent, DatasetDTO datasetDTO, AbstractSqlAssembler sqlAssembler, List<Segment> pushDownFilters) {
        this.metaConfigContent = metaConfigContent;
        this.datasetDTO = datasetDTO;
        this.sqlAssembler = sqlAssembler;
        this.pushDownFilters = pushDownFilters;
    }

    public String produceTableSql() {
        List<TableDescriptor> tables = metaConfigContent.getTables();

        if (metaConfigContent.isSingleTable()) {
            TableDescriptor table = tables.get(0);
            return String.format("(%s) %s", getBasicTableSql(table), table.getAlias());
        }

        List<String> columnsWithAlias = getColumnWithAlias(tables);

        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(Joiner.on(" , ").join(columnsWithAlias));
        sql.append(" from ");
        Set<String> joinedTable = Sets.newHashSet();
        Map<String, TableDescriptor> tableMap = tables.stream().collect(Collectors.toMap(t -> t.getAlias(), t -> t));
        List<TableJoinDescriptor> joinDescriptors = metaConfigContent.getJoinDescriptors();
        for (TableJoinDescriptor tableJoinDescriptor : joinDescriptors) {
            boolean isFirst = joinedTable.isEmpty();
            if (!isFirst) {
                sql.append(tableJoinDescriptor.getJoinType().getExpression());
            }
            String leftTableAlias = tableJoinDescriptor.getLeftTableAlias();
            if (!joinedTable.contains(leftTableAlias)) {
                TableDescriptor table = tableMap.get(leftTableAlias);
                String basicTableSql = getBasicTableSql(table);
                sql.append(String.format("(%s) %s", basicTableSql, table.getAlias()));
                joinedTable.add(table.getAlias());
            }

            if (isFirst) {
                sql.append(tableJoinDescriptor.getJoinType().getExpression());
            }

            String rightTableAlias = tableJoinDescriptor.getRightTableAlias();
            if (!joinedTable.contains(rightTableAlias)) {
                TableDescriptor table = tableMap.get(rightTableAlias);
                String basicTableSql = getBasicTableSql(table);
                sql.append(String.format("(%s) %s", basicTableSql, table.getAlias()));
                joinedTable.add(table.getAlias());
            }

            if (!Strings.isNullOrEmpty(tableJoinDescriptor.getJoinExpression())) {
                sql.append(" on ").append(tableJoinDescriptor.getJoinExpression());
            }
        }
        return sql.toString();
    }

    public String producePreviewSql() {

        String innerTableSql = produceTableSql();

        Optional<DatasetFieldDTO> firstDimension = datasetDTO.getFields().stream().filter(field -> field.getCategory() == FieldCategoryEnum.DIMENSION).findFirst();
        Optional<DatasetFieldDTO> firstMeasure = datasetDTO.getFields().stream().filter(field -> field.getCategory() == FieldCategoryEnum.MEASURE).findFirst();

        List<Segment> segments = Lists.newArrayList();

        if (firstDimension.isPresent()) {
            segments.add(new Segment(firstDimension.get().getName()));
        }

        if (firstMeasure.isPresent()) {
            DatasetFieldDTO field = firstMeasure.get();
            Measure measure = new Measure();
            measure.setName(field.getName());
            measure.setAggregator(field.getAggregator());
            measure.setExpression(field.getExpression());
            measure.setDataType(field.getDataType().name());

            segments.add(sqlAssembler.parseMeasure(measure));
        }

        StringBuilder sql = new StringBuilder();
        sql.append("select ");
        sql.append(Joiner.on(" , ").join(segments.stream().map(segment -> segment.getExpressionWithAlias()).collect(Collectors.toList())));
        sql.append(" from (").append(innerTableSql);
        sql.append(") as tmp");

        if (firstDimension.isPresent()) {
            DatasetFieldDTO field = firstMeasure.get();
            sql.append(" where ");
            sql.append(field.getName()).append(" = '").append(Mocks.mockValue(field.getDataType())).append("'");

            sql.append(" group by ").append(field.getName());
        }

        sql.append(" limit 10000");
        return sql.toString();
    }

    private String getBasicTableSql(TableDescriptor tableDescriptor) {

        Set<String> columns = Sets.newHashSet();
        columns.addAll(tableDescriptor.getColumns());

        Set<String> joinFields = Optional.ofNullable(metaConfigContent.getJoinDescriptors()).orElseGet(Lists::newArrayList)
                .stream()
                .flatMap(joinDescriptor -> joinDescriptor.getJoinFieldByAlias(tableDescriptor.getAlias()).stream())
                .collect(Collectors.toSet());

        columns.addAll(joinFields);

        StringBuilder sql = new StringBuilder();
        sql.append("select ");

        sql.append(Joiner.on(" , ").join(columns.stream().map(col -> String.format("`%s`", col)).collect(Collectors.toList())));

        sql.append(" from ").append(tableDescriptor.getDbName()).append(".").append(tableDescriptor.getTableName());

        List<String> wheres = Lists.newArrayList();


        if (Objects.nonNull(tableDescriptor.getFilters())) {

            String where = tableDescriptor.getFilters().parseFilterCondition(datasetDTO);

            if (!Strings.isNullOrEmpty(where)) {
                wheres.add(String.format("(%s)", where));
            }
        }

        List<Segment> needPushDownFilter = getNeedPushDownFilter();
        if (!needPushDownFilter.isEmpty()) {
            Set<String> joinColumns = metaConfigContent.getAllJoinColumnsByAlias(tableDescriptor.getAlias());

            List<String> filterWheres = needPushDownFilter.stream()
                    .filter(f -> joinColumns.contains(f.getName()))
                    .filter(Objects::nonNull)
                    .map(Segment::getExpression)
                    .map(s -> String.format("(%s)", s))
                    .collect(Collectors.toList());

            wheres.addAll(filterWheres);
        }

        if (!wheres.isEmpty()) {
            sql.append(" where ").append(Joiner.on(" and ").join(wheres));
        }

        return sql.toString();
    }

    private List<String> getColumnWithAlias(List<TableDescriptor> tables) {


        Map<String, String> fieldAliasMap = datasetDTO.getFields().stream()
                .filter(field -> !field.getType().isNewAdd())
                .collect(Collectors.
                        toMap(field -> String.format("%s.`%s`", field.getTableAlias(), field.getSourceFieldName()), DatasetFieldDTO::getName));

        List<String> columnWithAlias = Lists.newArrayList();

        for (TableDescriptor table : tables) {
            for (String column : table.getColumns()) {
                String originField = String.format("%s.`%s`", table.getAlias(), column);
                String alias = fieldAliasMap.get(originField);
                if (!Strings.isNullOrEmpty(alias)) {
                    columnWithAlias.add(String.format("%s as `%s`", originField, alias));
                }
            }
        }
        return columnWithAlias;
    }

    private List<Segment> getNeedPushDownFilter() {

        if (Objects.nonNull(pushDownFilters)) {
            return pushDownFilters;
        }

        QueryContext queryContext = sqlAssembler.getQueryContext();
        if (Objects.isNull(queryContext)) {
            return Lists.newArrayList();
        }
        QueryParam queryParam = queryContext.getQueryParam();
        if (Objects.isNull(queryParam)) {
            return Lists.newArrayList();
        }
        List<Filter> filters = queryParam.getFilters();
        if (Objects.isNull(filters)) {
            return Lists.newArrayList();
        }

        return filters.stream().map(sqlAssembler::parseFilter).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
