package net.aopacloud.superbi.model.query;

import lombok.Data;
import net.aopacloud.superbi.util.FieldUtils;

import java.util.List;

/**
 * @author: hudong
 * @date: 2023/8/9
 * @description:
 */
@Data
public class BaseQuery {

    /**
     * workspace id
     */
    private Long workspaceId;

    /**
     * sort field
     */
    protected String sortField;

    /**
     * sort type, eg: asc, desc
     */
    protected String sortType;

    /**
     * search keyword
     */
    private String keyword;

    /**
     * search users
     */
    private List<String> searchUsers;

    public String getSortField() {
        return sortField == null ? "" : FieldUtils.toUnderline(sortField);
    }

    public String getKeyword() {
        return keyword == null ? null : keyword.trim();
    }
}
