package net.aopacloud.superbi.model.query;

import lombok.Data;
import net.aopacloud.superbi.enums.AuthorizeScopeEnum;
import net.aopacloud.superbi.enums.PrivilegeTypeEnum;
import net.aopacloud.superbi.model.domain.Rows;
import net.aopacloud.superbi.model.vo.DatasetAuthorizeVO;

import java.util.List;

@Data
public class DatasetAuthorizeSaveVO {

    private Long id;

    private AuthorizeScopeEnum scope;

    private List<String> usernames;

    private List<Long> roleIds;

    private Long datasetId;

    private PrivilegeTypeEnum privilegeType;

    private Integer expireDuration;

    private String columnPrivilege;

    private Rows rows;

    // 新增字段默认授权
    private Boolean autoAuth = false;

}
