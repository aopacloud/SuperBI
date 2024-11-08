package net.aopacloud.superbi.model.query;

import lombok.Data;
import lombok.NonNull;
import net.aopacloud.superbi.enums.PositionEnum;
import net.aopacloud.superbi.enums.StatusEnum;
import net.aopacloud.superbi.util.FieldUtils;

import java.util.List;
import java.util.Set;

/**
 * @author: yan.zu
 * @date: 2024/10/16
 **/
@Data
public class ResourceQuery extends BaseQuery {

    private String keyword;

    //keyword翻译而来
    private List<String> searchUsers;

    @NonNull
    private PositionEnum position;

    private String creators;

    private List<String> creatorList;

    private String status;

    private List<String> statusList;

    private List<Long> workspaceIdList;

    private String workspaceIds;

    //空间管理员
    private List<Long> sourceWorkspaceIdList;

    private String dashboards;

    private List<Long> dashboardList;

    private Set<Long> sourceIdSet;

    private Set<Long> datasetIdSet;

    private Set<Long> reportIdSet;

    private String sortField;

    private String sortType;

    public String getSortField() {
        return sortField == null ? "" : FieldUtils.toUnderline(sortField);
    }
}
