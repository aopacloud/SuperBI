package net.aopacloud.superbi.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import net.aopacloud.superbi.enums.AuthorizeScopeEnum;
import net.aopacloud.superbi.enums.PermissionEnum;
import net.aopacloud.superbi.enums.PrivilegeTypeEnum;

import java.util.Date;

/**
 * @author: hudong
 * @date: 2023/9/6
 * @description:
 */
@Data
@Accessors(chain = true)
public class DatasetAuthorizeDTO {

    private Long id;

    private AuthorizeScopeEnum scope;

    private PermissionEnum permission;

    private String username;

    private String aliasName;

    private Long roleId;

    private String roleName;

    private Long datasetId;

    private String datasetName;

    private String datasetCreator;

    private PrivilegeTypeEnum privilegeType;

    private String operator;

    private Date startTime;

    private Integer expireDuration;

    private String columnPrivilege;

    private String rowPrivilege;

    private String rowParam;

    private String remark;

    private Integer deleted;

    private Date createTime;

    private Date updateTime;

    private Long workspaceId;

    private String workspaceName;

    private Date expireTime;

    public boolean isExpire() {
        if (expireDuration == null || expireDuration == 0) {
            return Boolean.FALSE;
        }
        long expireTime = startTime.getTime() + (expireDuration * 1000L);
        if (expireTime < new Date().getTime()) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

}
