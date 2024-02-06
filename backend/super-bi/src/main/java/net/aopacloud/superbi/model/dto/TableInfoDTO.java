package net.aopacloud.superbi.model.dto;

import lombok.Data;
import net.aopacloud.superbi.enums.EngineEnum;

import java.util.List;

@Data
public class TableInfoDTO {

    private EngineEnum engine = EngineEnum.CLICKHOUSE;

    private Long workspaceId;

    private String datasourceName;

    private String dbName;

    private String tableName;

    private String description;

    private Integer datasetCount;

    private List<FieldDTO> fields;

}
