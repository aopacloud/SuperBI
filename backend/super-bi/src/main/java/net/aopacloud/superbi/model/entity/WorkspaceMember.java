package net.aopacloud.superbi.model.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author: hudong
 * @date: 2023/8/4
 * @description:
 */
@Data
public class WorkspaceMember {

    private Long id;

    private Long workspaceId;

    private String username;

    private String level;

    private Long sysRoleId;

    private Date joinTime;

    private Date createTime;

    private Date updateTime;

}
