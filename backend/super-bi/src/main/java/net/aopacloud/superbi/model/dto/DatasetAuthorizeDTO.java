package net.aopacloud.superbi.model.dto;

import net.aopacloud.superbi.enums.AuthorizeScopeEnum;
import net.aopacloud.superbi.enums.PermissionEnum;
import net.aopacloud.superbi.enums.PrivilegeTypeEnum;
import lombok.Data;

import java.util.Date;

/**
 * @author: hudong
 * @date: 2023/9/6
 * @description:
 */
@Data
public class DatasetAuthorizeDTO {

    private Long id;

    private AuthorizeScopeEnum scope;

    private PermissionEnum permission;

    private String username;

    private String aliasName;

    private Long roleId;

    private String roleName;

    private Long datasetId;

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

}
