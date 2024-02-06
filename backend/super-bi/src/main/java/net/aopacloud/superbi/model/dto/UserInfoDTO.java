package net.aopacloud.superbi.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

/**
 * @author: hudong
 * @date: 2023/8/9
 * @description:
 */
@Data
@Accessors(chain = true)
public class UserInfoDTO {

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

    private List<WorkspaceDTO> workspaces;

    private Boolean isSuperAdmin;

}
