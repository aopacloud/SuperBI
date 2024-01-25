package net.aopacloud.superbi.queryEngine.model;

import net.aopacloud.superbi.common.core.context.SecurityContextHolder;
import net.aopacloud.superbi.enums.EngineEnum;
import net.aopacloud.superbi.model.dto.ConnectionParamDTO;
import net.aopacloud.superbi.model.dto.DatasetDTO;
import net.aopacloud.superbi.model.dto.DatasetFieldDTO;
import net.aopacloud.superbi.model.dto.TablePartition;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;
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

    private String username = SecurityContextHolder.getUserName();

    private String token = SecurityContextHolder.getToken();

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
        fieldMap = dataset.getFields().stream().collect(Collectors.toMap(DatasetFieldDTO::getName, field -> field));
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


    public String getTable() {
        return String.format("%s.%s", dataset.getConfig().getDbName(), dataset.getConfig().getTableName());
    }

    public EngineEnum getEngine() {
        return connectionParam.getEngine();
    }

}
