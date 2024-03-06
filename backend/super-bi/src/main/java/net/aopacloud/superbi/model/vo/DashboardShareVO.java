package net.aopacloud.superbi.model.vo;

import lombok.Data;
import net.aopacloud.superbi.enums.AuthorizeScopeEnum;
import net.aopacloud.superbi.enums.PermissionEnum;

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

    private String dashboardName;

    private String dashboardCreator;

    private String dashboardCreatorAlias;

    private Long workspaceId;

    private String workspaceName;

    private AuthorizeScopeEnum type;

    private Long roleId;

    private String roleName;

    private Date createTime;

    private Date updateTime;

    private int datasetCount;

    private int authorizeDatasetCount;
}