package net.aopacloud.superbi.model.dto;

import net.aopacloud.superbi.enums.EngineEnum;
import lombok.Data;

import java.util.Date;

/**
 * @author: hudong
 * @date: 2023/8/16
 * @description:
 */
@Data
public class DatasourceDTO {
    private Long id;

    private Long workspaceId;

    private EngineEnum engine;

    private String version;

    private String name;

    private String creator;

    private String url;

    private String host;

    private Integer port;

    private String user;

    private String password;

    private String database;

    private Boolean sslEnable;

    private String initSql;

    private Date createTime;

    private Date updateTime;
}