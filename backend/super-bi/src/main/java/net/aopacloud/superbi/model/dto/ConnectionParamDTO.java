package net.aopacloud.superbi.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import net.aopacloud.superbi.enums.EngineEnum;

/**
 * @author: hudong
 * @date: 2023/10/23
 * @description:
 */
@Data
@Accessors(chain = true)
public class ConnectionParamDTO {

    private EngineEnum engine;

    private String version;

    private String name;

    private String url;

    private String user;

    private String password;

    private String database;

    private String initSql;

}
