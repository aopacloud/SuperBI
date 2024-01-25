package net.aopacloud.superbi.model.vo;

import net.aopacloud.superbi.enums.AuthorizeScopeEnum;
import net.aopacloud.superbi.enums.PermissionEnum;
import lombok.Data;

import java.util.Date;

@Data
public class DashboardShareVO {
    private Long id;

    private String username;

    private String usernameAlias;

    private PermissionEnum permission;

    private String operator;

    private String operatorAlias;

    private Long dashboardId;

    private AuthorizeScopeEnum type;

    private Long roleId;

    private String roleName;

    private Date createTime;

    private Date updateTime;

}