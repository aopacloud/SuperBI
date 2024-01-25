package net.aopacloud.superbi.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author: hudong
 * @date: 2023/8/4
 * @description:
 */
@Data
public class WorkspaceMemberDTO {

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
