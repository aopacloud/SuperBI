package net.aopacloud.superbi.model.query;

import lombok.Data;
import net.aopacloud.superbi.enums.AuthorizeScopeEnum;
import net.aopacloud.superbi.enums.PermissionEnum;
import net.aopacloud.superbi.enums.PrivilegeTypeEnum;

import java.util.List;

@Data
public class DatasetAuthorizeBatchQuery extends BaseQuery {

    private String creatorKeyword;

    private String[] creators;

    private AuthorizeScopeEnum[] scopes;

    private String userAndRoleKeyword;

    private String userAndRoles[];

    private String[] usernames;

    private Long[] roleIds;

    private PermissionEnum[] permissions;

    private PrivilegeTypeEnum[] privilegeTypes;

    private Long[] workspaceIds;

    private List<Long> datasetIds;
}
