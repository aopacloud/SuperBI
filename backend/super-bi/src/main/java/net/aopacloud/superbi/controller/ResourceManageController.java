package net.aopacloud.superbi.controller;

import lombok.RequiredArgsConstructor;
import net.aopacloud.superbi.common.core.web.domain.RestApiResponse;
import net.aopacloud.superbi.common.core.web.page.PageVO;
import net.aopacloud.superbi.enums.PositionEnum;
import net.aopacloud.superbi.model.query.ResourceQuery;
import net.aopacloud.superbi.model.vo.FolderResourceRelationshipVO;
import net.aopacloud.superbi.model.vo.ReportVO;
import net.aopacloud.superbi.model.vo.ResourceVO;
import net.aopacloud.superbi.service.ResourceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 资源管理
 *
 * @author: yan.zu
 * @date: 2024/10/22
 **/
@RestController
@RequiredArgsConstructor
@RequestMapping("resource/manage")
public class ResourceManageController {

    private final ResourceService resourceService;


    /**
     * 资源管理搜索
     *
     * @param resourceQuery
     * @return
     */
    @GetMapping
    public RestApiResponse<PageVO<ResourceVO>> search(ResourceQuery resourceQuery) {
        PageVO<ResourceVO> pageVO = resourceService.search(resourceQuery);
        return RestApiResponse.success(pageVO);
    }


    /**
     * 图表列表
     *
     * @param id
     * @param position
     * @return
     */
    @GetMapping("/report")
    public RestApiResponse<List<ReportVO>> getReport(Long id, PositionEnum position) {
        List<ReportVO> reportVOList = resourceService.getReport(id, position);
        return RestApiResponse.success(reportVOList);
    }

    /**
     * 批量删除
     *
     * @param idList
     * @param position
     * @return
     */
    @DeleteMapping("/delete")
    public RestApiResponse<String> delete(@RequestBody List<Long> idList, @RequestParam PositionEnum position) {
        resourceService.delete(idList, position);
        return RestApiResponse.success("ok");
    }

    /**
     * 批量下线
     *
     * @param idList
     * @param position
     * @return
     */
    @PostMapping("/offline")
    public RestApiResponse<String> offline(@RequestBody List<Long> idList, @RequestParam PositionEnum position) {
        resourceService.offline(idList, position);
        return RestApiResponse.success("ok");
    }

    /**
     * 批量上线
     *
     * @param idList
     * @param position
     * @return
     */
    @PostMapping("/online")
    public RestApiResponse<String> online(@RequestBody List<Long> idList, @RequestParam PositionEnum position) {
        resourceService.online(idList, position);
        return RestApiResponse.success("ok");
    }

    /**
     * 批量发布
     *
     * @param idList
     * @param position
     * @return
     */
    @PostMapping("/publish")
    public RestApiResponse<String> publish(@RequestBody List<Long> idList, @RequestParam PositionEnum position) {
        resourceService.publish(idList, position);
        return RestApiResponse.success("ok");
    }

    /**
     * 批量移动资源
     *
     * @param folderResourceRelList
     * @return
     */
    @PostMapping("/moveResourceList")
    public RestApiResponse<List<FolderResourceRelationshipVO>> moveResource(@RequestBody List<FolderResourceRelationshipVO> folderResourceRelList) {
        resourceService.moveResource(folderResourceRelList);
        return RestApiResponse.success(folderResourceRelList);
    }
}
