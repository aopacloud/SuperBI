package net.aopacloud.superbi.model.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author: hudong
 * @date: 2023/8/9
 * @description:
 */
@Data
public class UserInfoVO {
    private Long id;

    private String username;

    private String name;

    private String aliasName;

    private String avatar;

    private String email;

    private String mobile;

    private Date accountExpireTime;

    private Date createTime;

    private Date updateTime;

    private List<WorkspaceVO> workspaces;

    private Boolean isSuperAdmin;
}
