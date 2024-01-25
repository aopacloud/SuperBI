package net.aopacloud.superbi.model.query;

import net.aopacloud.superbi.enums.AuthorizeScopeEnum;
import net.aopacloud.superbi.enums.PrivilegeTypeEnum;
import net.aopacloud.superbi.model.vo.DatasetAuthorizeVO;
import lombok.Data;

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

    private DatasetAuthorizeVO.Rows rows;
}
