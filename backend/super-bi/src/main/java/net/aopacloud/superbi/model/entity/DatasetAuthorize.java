package net.aopacloud.superbi.model.entity;

import lombok.Data;
import net.aopacloud.superbi.enums.AuthorizeScopeEnum;
import net.aopacloud.superbi.enums.PermissionEnum;
import net.aopacloud.superbi.enums.PrivilegeTypeEnum;

import java.util.Date;

/**
 * @author: hudong
 * @date: 2023/9/5
 * @description:
 */
@Data
public class DatasetAuthorize {

    private Long id;

    private AuthorizeScopeEnum scope;

    private PermissionEnum permission;

    private String username;

    private Long roleId;

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
