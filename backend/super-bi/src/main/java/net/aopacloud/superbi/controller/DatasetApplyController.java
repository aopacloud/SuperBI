package net.aopacloud.superbi.controller;

import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import net.aopacloud.superbi.common.core.context.SecurityContextHolder;
import net.aopacloud.superbi.common.core.utils.PageUtils;
import net.aopacloud.superbi.common.core.web.domain.RestApiResponse;
import net.aopacloud.superbi.common.core.web.page.PageVO;
import net.aopacloud.superbi.model.converter.ApplyMessageConverter;
import net.aopacloud.superbi.model.converter.DatasetApplyConverter;
import net.aopacloud.superbi.model.dto.ApplyMessageDTO;
import net.aopacloud.superbi.model.dto.DatasetApplyDTO;
import net.aopacloud.superbi.model.query.DatasetApplyQuery;
import net.aopacloud.superbi.model.vo.ApplyMessageVO;
import net.aopacloud.superbi.model.vo.DatasetApplyVO;
import net.aopacloud.superbi.service.DatasetApplyService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Dataset Apply
 * @author: hudong
 * @date: 2023/9/6
 * @description:
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("datasetApply")
public class DatasetApplyController {

    private final DatasetApplyService datasetApplyService;

    private final DatasetApplyConverter converter;

    private final ApplyMessageConverter applyMessageConverter;

    /**
     * myself apply list
     * @param query
     * @return
     */
    @GetMapping("/myApply")
    public RestApiResponse<PageVO<DatasetApplyVO>> myApplyList(DatasetApplyQuery query){

        query.setUsername(SecurityContextHolder.getUserName());
        PageUtils.startPage();
        List<DatasetApplyDTO> applies = datasetApplyService.search(query);
        PageInfo pageInfo = new PageInfo(applies);

        PageVO pageVO = new PageVO(converter.toVOList(applies), pageInfo.getTotal());
        return RestApiResponse.success(pageVO);
    }
    /**
     * myself approve list
     * @param query
     * @return
     */
    @GetMapping("/myApprove")
    public RestApiResponse<PageVO<DatasetApplyVO>> myApproveList(DatasetApplyQuery query){

        query.setDatasetCreator(SecurityContextHolder.getUserName());
        PageUtils.startPage();
        List<DatasetApplyDTO> applies = datasetApplyService.search(query);
        PageInfo pageInfo = new PageInfo(applies);

        PageVO pageVO = new PageVO(converter.toVOList(applies), pageInfo.getTotal());
        return RestApiResponse.success(pageVO);
    }

    /**
     * apply manage list
     * administrator managed apply list
     * @param query
     * @return
     */
    @GetMapping("/applyManage")
    public RestApiResponse<PageVO<DatasetApplyVO>> applyManageList(DatasetApplyQuery query){

        query.setDatasetCreator(SecurityContextHolder.getUserName());
        PageUtils.startPage();
        List<DatasetApplyDTO> applies = datasetApplyService.search(query);
        PageInfo pageInfo = new PageInfo(applies);

        PageVO pageVO = new PageVO(converter.toVOList(applies), pageInfo.getTotal());
        return RestApiResponse.success(pageVO);
    }

    /**
     * post apply
     * @param datasetApplyVO
     * @return
     */
    @PostMapping
    public RestApiResponse<DatasetApplyVO> newApply(@RequestBody DatasetApplyVO datasetApplyVO) {

        DatasetApplyDTO datasetApplyDTO = datasetApplyService.postApply(converter.toDTO(datasetApplyVO));

        return RestApiResponse.success(converter.toVO(datasetApplyDTO));
    }

    /**
     * update apply
     * @param datasetApplyVO
     * @param id
     * @return
     */
    @PutMapping("/{id}")
    public RestApiResponse<DatasetApplyVO> updateApply(@RequestBody DatasetApplyVO datasetApplyVO, @PathVariable Long id) {

        datasetApplyVO.setId(id);

        DatasetApplyDTO datasetApplyDTO = datasetApplyService.updateApply(converter.toDTO(datasetApplyVO));

        return RestApiResponse.success(converter.toVO(datasetApplyDTO));
    }

    /**
     * revoke apply
     * proposer revoke apply
     * @param id
     * @return
     */
    @PostMapping("/{id}/revoke")
    public RestApiResponse<DatasetApplyVO> revokeApply(@PathVariable Long id) {
        DatasetApplyDTO datasetApplyDTO = datasetApplyService.revokeApply(id);
        return RestApiResponse.success(converter.toVO(datasetApplyDTO));
    }

    /**
     * reject apply
     * dataset creator reject apply
     * @param id apply id
     * @param datasetApplyVO
     * @return
     */
    @PostMapping("/{id}/reject")
    public RestApiResponse<DatasetApplyVO> rejectApply(@RequestBody DatasetApplyVO datasetApplyVO, @PathVariable Long id) {
        datasetApplyVO.setId(id);

        DatasetApplyDTO datasetApplyDTO = datasetApplyService.rejectApply(converter.toDTO(datasetApplyVO));

        return RestApiResponse.success(converter.toVO(datasetApplyDTO));
    }

    @PostMapping("/{id}/disagree")
    public RestApiResponse<DatasetApplyVO> disagreeApply(@RequestBody DatasetApplyVO datasetApplyVO,@PathVariable Long id ) {
        datasetApplyVO.setId(id);
        DatasetApplyDTO datasetApplyDTO = datasetApplyService.disagree(converter.toDTO(datasetApplyVO));

        return RestApiResponse.success(converter.toVO(datasetApplyDTO));
    }

    /**
     * pass apply
     * dataset creator pass apply
     * @param id
     * @return
     */
    @PostMapping("/{id}/pass")
    public RestApiResponse<DatasetApplyVO> passApply(@PathVariable Long id){

        DatasetApplyDTO datasetApplyDTO = datasetApplyService.passApply(id);

        return RestApiResponse.success(converter.toVO(datasetApplyDTO));
    }


    /**
     * get specified apply
     * @param id apply id
     * @return return specified apply
     */
    @GetMapping("/{id}")
    public RestApiResponse<DatasetApplyVO> findOne(@PathVariable Long id) {

        DatasetApplyDTO datasetApplyDTO = datasetApplyService.findOne(id);
        return RestApiResponse.success(converter.toVO(datasetApplyDTO));
    }

    @GetMapping("/applyMessage")
    public RestApiResponse<ApplyMessageVO> getApplyMessage() {

        ApplyMessageDTO applyMessageDTO = datasetApplyService.getApplyMessage();

        return RestApiResponse.success(applyMessageConverter.toVO(applyMessageDTO));
    }
}
