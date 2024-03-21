package net.aopacloud.superbi.model.vo;

import lombok.Data;
import net.aopacloud.superbi.enums.AuthorizeScopeEnum;
import net.aopacloud.superbi.enums.PermissionEnum;
import net.aopacloud.superbi.enums.PrivilegeTypeEnum;
import net.aopacloud.superbi.queryEngine.sql.operator.FunctionalOperatorEnum;

import java.util.Date;
import java.util.List;

/**
 * @author: hudong
 * @date: 2023/9/6
 * @description:
 */
@Data
public class DatasetAuthorizeVO {
    private Long id;

    private AuthorizeScopeEnum scope;

    private PermissionEnum permission;

    private String username;

    private String usernameAlias;

    private Long roleId;

    private String roleName;

    private Long datasetId;

    private String datasetName;

    private PrivilegeTypeEnum privilegeType;

    private String operator;

    private String operatorAlias;

    private Date startTime;

    private Integer expireDuration;

    private String columnPrivilege;

    private String rowPrivilege;

    private String datasetCreator;

    private String datasetCreatorAlias;

    private Long workspaceId;

    private String workspaceName;

    private Rows rows;

    private String remark;

    private Integer deleted;

    private Date createTime;

    private Date updateTime;

    private Date expireTime;

    @Data
    public static class Row {
        private String relation;
        private List<Condition> children;
    }

    @Data
    public static class Condition {
        private String dataType;
        private String field;
        private FunctionalOperatorEnum operator;
        private String type;
        private String databaseDataType;
        private List<String> value;
    }

    @Data
    public static class Rows {
        private String relation;
        private List<Row> children;
    }
}
