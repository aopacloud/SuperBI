package net.aopacloud.superbi.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.aopacloud.superbi.enums.EngineEnum;

/**
 * @author: hu.dong
 * @date: 2021/11/25
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseDTO {

    private Long workspaceId;

    private EngineEnum engine;

    private String datasourceName;

    private String dbName;

}
