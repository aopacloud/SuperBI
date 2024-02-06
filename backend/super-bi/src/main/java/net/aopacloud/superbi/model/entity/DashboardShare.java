package net.aopacloud.superbi.model.entity;

import lombok.Data;
import net.aopacloud.superbi.enums.AuthorizeScopeEnum;
import net.aopacloud.superbi.enums.PermissionEnum;

import java.util.Date;

@Data
public class DashboardShare {

    private Long id;

    private String username;

    private PermissionEnum permission;

    private String operator;

    private Long dashboardId;

    private AuthorizeScopeEnum type;

    private Long roleId;

    private Date createTime;

    private Date updateTime;

}