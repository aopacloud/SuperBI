package net.aopacloud.superbi.service;

import net.aopacloud.superbi.common.core.web.page.PageVO;
import net.aopacloud.superbi.enums.PositionEnum;
import net.aopacloud.superbi.model.query.RecycleQuery;
import net.aopacloud.superbi.model.vo.RecycleVO;

import java.util.List;

/**
 * 回收站
 *
 * @author: yan.zu
 * @date: 2024/10/16
 **/
public interface RecycleService {

    PageVO<RecycleVO> search(RecycleQuery recycleQuery);

    void delete(PositionEnum position, List<Long> idList);

    void restore(PositionEnum position, Boolean autoRestore, List<Long> idList);
}
