package net.aopacloud.superbi.model.query;

import lombok.Data;
import net.aopacloud.superbi.constant.BiConsist;
import net.aopacloud.superbi.util.FieldUtils;

import java.util.List;
import java.util.Objects;

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

    private Integer pageNum;

    private Integer pageSize;

    public String getSortField() {
        return sortField == null ? "" : FieldUtils.toUnderline(sortField);
    }

    public String getKeyword() {
        return keyword == null ? null : keyword.trim();
    }

    public Integer getPageNum() {
        if (Objects.isNull(this.pageNum) || this.pageNum == 0) {
            return BiConsist.DEFAULT_PAGE_NUM;
        }
        return this.pageNum;
    }

    public Integer getPageSize() {
        if(Objects.isNull(this.pageSize) || this.pageSize == 0) {
            return BiConsist.DEFAULT_PAGE_SIZE;
        }
        return this.pageSize;
    }
}
