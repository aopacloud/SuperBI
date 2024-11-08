package net.aopacloud.superbi.model.vo;

import lombok.Data;
import net.aopacloud.superbi.enums.EngineEnum;
import net.aopacloud.superbi.model.domain.MetaConfigContent;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author: hudong
 * @date: 2023/8/15
 * @description:
 */
@Data
public class DatasetMetaConfigVO {

    private Long id;

    private Long datasetId;

    private Integer version;

    private Long datasourceId;

    @NotNull
    private String datasourceName;

    private EngineEnum engine;

    @NotNull
    private String dbName;

    @NotNull
    private String tableName;

    private MetaConfigContent content;

    private String sql;

    private Date createTime;

    private Date updateTime;

    private String refTables;

    private String layout;
}
