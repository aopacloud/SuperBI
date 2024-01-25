package net.aopacloud.superbi.model.vo;

import net.aopacloud.superbi.enums.EngineEnum;
import lombok.Data;

/**
 * @author: hudong
 * @date: 2023/10/23
 * @description:
 */
@Data
public class DatabaseVO {

    private Long workspaceId;

    private EngineEnum engine;

    private String datasourceName;

    private String dbName;

}
