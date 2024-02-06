package net.aopacloud.superbi.model.vo;

import lombok.Data;
import net.aopacloud.superbi.enums.EngineEnum;

import java.util.Date;

/**
 * @Author shinnie
 * @Description
 * @Date 17:35 2023/8/25
 */
@Data
public class DatasourceVO {
    private Long id;

    private Long workspaceId;

    private EngineEnum engine;

    private String version;

    private String name;

    private String creator;

    private String creatorAlias;

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
