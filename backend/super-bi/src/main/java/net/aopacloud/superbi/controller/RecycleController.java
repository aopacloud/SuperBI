package net.aopacloud.superbi.controller;

import lombok.RequiredArgsConstructor;
import net.aopacloud.superbi.common.core.web.domain.RestApiResponse;
import net.aopacloud.superbi.common.core.web.page.PageVO;
import net.aopacloud.superbi.enums.PositionEnum;
import net.aopacloud.superbi.model.query.RecycleQuery;
import net.aopacloud.superbi.model.vo.RecycleVO;
import net.aopacloud.superbi.service.RecycleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 回收站
 *
 * @author: yan.zu
 * @date: 2024/10/16
 **/
@RestController
@RequiredArgsConstructor
@RequestMapping("recycle")
public class RecycleController {

    private final RecycleService recycleService;


    /**
     * 回收站搜索
     *
     * @param recycleQuery
     * @return
     */
    @GetMapping
    public RestApiResponse<PageVO<RecycleVO>> search(RecycleQuery recycleQuery) {
        PageVO<RecycleVO> pageVO = recycleService.search(recycleQuery);
        return RestApiResponse.success(pageVO);
    }

    /**
     * 回收站删除
     *
     * @param idList
     * @return
     */
    @DeleteMapping("/delete")
    public RestApiResponse<RecycleVO> delete(@RequestParam PositionEnum position, @RequestBody List<Long> idList) {
        recycleService.delete(position, idList);
        return RestApiResponse.success();
    }

    /**
     * 回收站恢复
     *
     * @param idList
     * @return
     */
    @PostMapping("/restore")
    public RestApiResponse<RecycleVO> restore(@RequestParam PositionEnum position, @RequestParam Boolean autoRestore, @RequestBody List<Long> idList) {
        recycleService.restore(position, autoRestore, idList);
        return RestApiResponse.success();
    }
}
