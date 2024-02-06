package net.aopacloud.superbi.queryEngine.metadata;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.aopacloud.superbi.common.core.utils.bean.BeanUtils;
import net.aopacloud.superbi.model.dto.ConnectionParamDTO;
import net.aopacloud.superbi.model.dto.DatasourceDTO;
import net.aopacloud.superbi.model.dto.FieldDTO;
import net.aopacloud.superbi.model.dto.TableSchemaDTO;
import net.aopacloud.superbi.queryEngine.DataTypeConverter;
import net.aopacloud.superbi.queryEngine.executor.JdbcExecutor;
import net.aopacloud.superbi.queryEngine.executor.QueryExecutorFactory;
import net.aopacloud.superbi.utils.DatasourceHelper;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JdbcMetaDataBackend implements MetaDataBackend {

    private final QueryExecutorFactory queryExecutorFactory;

    @Override
    public List<TableSchemaDTO> getTableSchemas(DatasourceDTO datasourceDTO) {
        List<TableSchemaDTO> tableSchemaDTOS = Lists.newArrayList();
        try {
            Connection connection = getConnection(datasourceDTO);

            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet tableResultSet = metaData.getTables(connection.getCatalog(), connection.getCatalog() + "%", "%", new String[]{"TABLE"});
            while (tableResultSet.next()) {
                String tableName = tableResultSet.getString("TABLE_NAME");
                String remark = tableResultSet.getString("REMARKS");
                TableSchemaDTO tableSchema = new TableSchemaDTO();
                tableSchema.setTableName(tableName);
                tableSchema.setDescription(remark);
                tableSchema.setEngine(datasourceDTO.getEngine());
                tableSchema.setWorkspaceId(datasourceDTO.getWorkspaceId());
                tableSchema.setDatasourceName(datasourceDTO.getName());
                tableSchema.setDbName(connection.getCatalog());
                tableSchemaDTOS.add(tableSchema);
            }
        } catch (Exception e) {
            log.error("get tables error", e);
            throw new RuntimeException(e);
        }

        return tableSchemaDTOS;
    }

    @Override
    public TableSchemaDTO getTableSchema(DatasourceDTO datasourceDTO, String tableName) {
        TableSchemaDTO tableSchemaDTO = new TableSchemaDTO();
        try (Connection connection = getConnection(datasourceDTO);) {
            List<FieldDTO> fields = Lists.newArrayList();
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet columnResultSet = metaData.getColumns(connection.getCatalog(), "%", tableName, "%");
            while (columnResultSet.next()) {
                FieldDTO field = new FieldDTO();
                field.setName(columnResultSet.getString("COLUMN_NAME"));
                field.setDescription(columnResultSet.getString("REMARKS"));
                field.setDatabaseDataType(columnResultSet.getString("TYPE_NAME"));
                field.setDataType(DataTypeConverter.convert(columnResultSet.getString("TYPE_NAME")));
                fields.add(field);
            }

            tableSchemaDTO.setFields(fields);
            tableSchemaDTO.setTableName(tableName);
            tableSchemaDTO.setEngine(datasourceDTO.getEngine());
            tableSchemaDTO.setWorkspaceId(datasourceDTO.getWorkspaceId());
            tableSchemaDTO.setDatasourceName(datasourceDTO.getName());
            tableSchemaDTO.setDbName(datasourceDTO.getDatabase());

        } catch (Exception e) {
            log.error("get tables error", e);
            throw new RuntimeException(e);
        }
        return tableSchemaDTO;
    }

    public JdbcExecutor getJdbcExecutor(DatasourceDTO datasourceDTO) {
        return (JdbcExecutor) queryExecutorFactory.getQueryExecutor(datasourceDTO.getEngine());
    }

    public Connection getConnection(DatasourceDTO datasourceDTO) throws Exception {
        JdbcExecutor jdbcExecutor = getJdbcExecutor(datasourceDTO);
        ConnectionParamDTO param = new ConnectionParamDTO();
        BeanUtils.copyProperties(datasourceDTO, param);
        param.setUrl(DatasourceHelper.getConnectionUrl(datasourceDTO));
        Connection connection = jdbcExecutor.getConnection(param);
        return connection;
    }

}
