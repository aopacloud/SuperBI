package net.aopacloud.superbi.model.dto;

import lombok.Data;
import net.aopacloud.superbi.enums.EngineEnum;

import java.util.Date;

/**
 * @author: hudong
 * @date: 2023/8/15
 * @description:
 */
@Data
public class DatasetMetaConfigDTO {

    private Long id;

    private Long datasetId;

    private Integer version;

    private Long datasourceId;

    private String datasourceName;

    private EngineEnum engine;

    private String dbName;

    private String tableName;

    private String content;

    private String sql;

    private Date createTime;

    private Date updateTime;

}
