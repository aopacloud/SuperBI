package net.aopacloud.superbi.model.vo;

import lombok.Data;
import net.aopacloud.superbi.enums.EngineEnum;
import net.aopacloud.superbi.model.dto.FieldDTO;

import java.util.List;

/**
 * @author: hudong
 * @date: 2023/10/23
 * @description:
 */
@Data
public class TableSchemaVO {

    private EngineEnum engine = EngineEnum.CLICKHOUSE;

    private Long workspaceId;

    private String datasourceName;

    private String dbName;

    private String tableName;

    private String description;

    private List<FieldDTO> fields;

    public String getDbAndName() {
        return String.format("%s.%s", dbName, tableName);
    }

}
