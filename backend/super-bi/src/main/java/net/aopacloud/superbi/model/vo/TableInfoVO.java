package net.aopacloud.superbi.model.vo;

import net.aopacloud.superbi.enums.EngineEnum;
import net.aopacloud.superbi.model.dto.FieldDTO;
import lombok.Data;

import java.util.List;

@Data
public class TableInfoVO {

    private EngineEnum engine = EngineEnum.CLICKHOUSE;

    private Long workspaceId;

    private String datasourceName;

    private String dbName;

    private String tableName;

    private String description;

    private Integer datasetCount;

    private List<FieldDTO> fields;

}
