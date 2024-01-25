package net.aopacloud.superbi.model.query;

import net.aopacloud.superbi.common.core.context.SecurityContextHolder;
import net.aopacloud.superbi.enums.FolderTypeEnum;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * @author: hudong
 * @date: 2023/10/30
 * @description:
 */
@Data
public class ConditionQuery {

    private Set<Long> ids;

    private String username = SecurityContextHolder.getUserName();

    private FolderTypeEnum folderType;

    private boolean superAdmin;

    private Long workspaceId;

    private List<Long> folderIds;

    private String keyword;

    private List<String> searchUsernames;

    protected String sortField;

    protected String sortType;

    public static ConditionQuery from (BaseQuery query) {
        ConditionQuery conditionQuery = new ConditionQuery();
        conditionQuery.setKeyword(query.getKeyword());
        conditionQuery.setWorkspaceId(query.getWorkspaceId());
        conditionQuery.setSortField(query.getSortField());
        conditionQuery.setSortType(query.getSortType());
        return conditionQuery;
    }
}
