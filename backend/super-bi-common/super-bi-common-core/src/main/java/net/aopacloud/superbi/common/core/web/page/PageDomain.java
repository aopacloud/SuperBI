package net.aopacloud.superbi.common.core.web.page;

import lombok.Data;
import net.aopacloud.superbi.common.core.utils.StringUtils;

/**
 * pager data
 */
@Data
public class PageDomain {
    /**
     * current page number
     */
    private Integer pageNum;

    /**
     * data size per page
     */
    private Integer pageSize;

    /**
     * sort column
     */
    private String orderByColumn;

    /**
     * order direction "desc" or "asc"
     */
    private String isAsc = "asc";

    /**
     * page parameters reasonable
     */
    private Boolean reasonable = true;

    public String getOrderBy() {
        if (StringUtils.isEmpty(orderByColumn)) {
            return "";
        }
        return StringUtils.toUnderScoreCase(orderByColumn) + " " + isAsc;
    }

    public String getIsAsc() {
        return isAsc;
    }

    public void setIsAsc(String isAsc) {
        if (StringUtils.isNotEmpty(isAsc)) {
            // 兼容前端排序类型
            if ("ascending".equals(isAsc)) {
                isAsc = "asc";
            } else if ("descending".equals(isAsc)) {
                isAsc = "desc";
            }
            this.isAsc = isAsc;
        }
    }

    public Boolean getReasonable() {
        if (StringUtils.isNull(reasonable)) {
            return Boolean.TRUE;
        }
        return reasonable;
    }

}
