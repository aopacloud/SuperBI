package net.aopacloud.superbi.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * @author: hudong
 * @date: 2023/8/4
 * @description:
 */
@Data
public class WorkspaceMemberVO {

    private Long id;

    private Long workspaceId;

    private String username;

    private String aliasName;

    private String level;

    private Long sysRoleId;

    private Date joinTime;

    private Date createTime;

    private Date updateTime;

}
