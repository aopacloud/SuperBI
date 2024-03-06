package net.aopacloud.superbi.model.query;

import lombok.Data;
import net.aopacloud.superbi.enums.AuthorizeScopeEnum;

import java.util.List;

@Data
public class DashboardShareBatchQuery extends BaseQuery{

    private String creatorKeyword;

    private String[] creators;

    private AuthorizeScopeEnum[] types;

    private String userAndRoleKeyword;

    private String userAndRoles[];

    private String[] usernames;

    private Long[] roleIds;

    private Long[] workspaceIds;

    private List<Long> dashboardIds;

}
