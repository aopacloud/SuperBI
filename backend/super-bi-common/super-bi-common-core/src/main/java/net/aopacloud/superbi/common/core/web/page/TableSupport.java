package net.aopacloud.superbi.common.core.web.page;

import net.aopacloud.superbi.common.core.text.Convert;
import net.aopacloud.superbi.common.core.utils.ServletUtils;

/**
 * Page support
 */
public class TableSupport {
    /**
     * current page number
     */
    public static final String PAGE_NUM = "pageNum";

    /**
     * number of records per page
     */
    public static final String PAGE_SIZE = "pageSize";

    /**
     * sort column
     */
    public static final String ORDER_BY_COLUMN = "orderByColumn";

    /**
     * sort direction "desc" or "asc"
     */
    public static final String IS_ASC = "isAsc";

    /**
     * page parameter rationalization
     */
    public static final String REASONABLE = "reasonable";

    /**
     * get page parameters
     */
    public static PageDomain getPageDomain() {
        PageDomain pageDomain = new PageDomain();
        pageDomain.setPageNum(Convert.toInt(ServletUtils.getParameter(PAGE_NUM), 1));
        pageDomain.setPageSize(Convert.toInt(ServletUtils.getParameter(PAGE_SIZE), 10));
        pageDomain.setOrderByColumn(ServletUtils.getParameter(ORDER_BY_COLUMN));
        pageDomain.setIsAsc(ServletUtils.getParameter(IS_ASC));
        pageDomain.setReasonable(ServletUtils.getParameterToBool(REASONABLE));
        return pageDomain;
    }

    public static PageDomain buildPageRequest() {
        return getPageDomain();
    }
}
