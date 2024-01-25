package net.aopacloud.superbi.model.dto;

import net.aopacloud.superbi.enums.AuthorizeScopeEnum;
import net.aopacloud.superbi.enums.PermissionEnum;
import lombok.Data;

import java.util.Date;

@Data
public class DashboardShareDTO {
    private Long id;

    private String username;

    private PermissionEnum permission;

    private String operator;

    private Long dashboardId;

    private AuthorizeScopeEnum type;

    private Long roleId;

    private String roleName;

    private Date createTime;

    private Date updateTime;

}