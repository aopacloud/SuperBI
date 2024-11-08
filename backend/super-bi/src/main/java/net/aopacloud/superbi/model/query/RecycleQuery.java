package net.aopacloud.superbi.model.query;

import lombok.Data;
import net.aopacloud.superbi.enums.PositionEnum;
import net.aopacloud.superbi.util.FieldUtils;

import java.util.List;

/**
 * @author: yan.zu
 * @date: 2024/10/16
 **/
@Data
public class RecycleQuery extends BaseQuery {

    private String keyword;

    private PositionEnum position;

    private String creators;

    private String currentUser;

    private boolean superAdmin;

    private List<Long> workspaceAdminIdList;

    private List<String> creatorList;

    private String workspaceIds;

    private List<Long> workspaceIdList;

    private String sortField;

    private String sortType;

    public String getSortField() {
        return sortField == null ? "" : FieldUtils.toUnderline(sortField);
    }
}
