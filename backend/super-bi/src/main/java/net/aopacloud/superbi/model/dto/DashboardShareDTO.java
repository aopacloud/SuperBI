package net.aopacloud.superbi.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import net.aopacloud.superbi.enums.AuthorizeScopeEnum;
import net.aopacloud.superbi.enums.PermissionEnum;

import java.util.Date;

@Data
@Accessors(chain = true)
public class DashboardShareDTO {
    private Long id;

    private String username;

    private PermissionEnum permission;

    private String operator;

    private Long dashboardId;

    private String dashboardName;

    private String dashboardCreator;

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