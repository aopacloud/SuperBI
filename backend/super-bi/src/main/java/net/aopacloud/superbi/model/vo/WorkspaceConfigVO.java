package net.aopacloud.superbi.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * @author: hudong
 * @date: 2023/8/4
 * @description:
 */
@Data
public class WorkspaceConfigVO {

    private Long id;

    private Long workspaceId;

    private Integer shared;

    private String activeTimezone;

    private String selectedTimezone;

    private Date createTime;

    private Date updateTime;

}
