package net.aopacloud.superbi.common.core.utils;

import com.github.pagehelper.PageHelper;
import net.aopacloud.superbi.common.core.web.page.PageDomain;
import net.aopacloud.superbi.common.core.web.page.TableSupport;

/**
 * pager utils
 */
public class PageUtils extends PageHelper {
    /**
     * set page parameters
     */
    public static void startPage() {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        Boolean reasonable = pageDomain.getReasonable();
        PageHelper.startPage(pageNum, pageSize).setReasonable(reasonable);
    }
}
