package net.aopacloud.superbi.model.dto;

import net.aopacloud.superbi.enums.EngineEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author: hu.dong
 * @date: 2021/10/20
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class TableSchemaDTO {

    private EngineEnum engine = EngineEnum.CLICKHOUSE;

    private Long workspaceId;

    private String datasourceName;

    private String dbName;

    private String tableName;

    private String description;

    private List<FieldDTO> fields;

}
