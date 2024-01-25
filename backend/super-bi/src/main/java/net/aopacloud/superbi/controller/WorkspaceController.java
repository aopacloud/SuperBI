package net.aopacloud.superbi.controller;

import net.aopacloud.superbi.common.core.web.domain.RestApiResponse;
import net.aopacloud.superbi.model.converter.WorkspaceConverter;
import net.aopacloud.superbi.model.converter.WorkspaceUserResourceConverter;
import net.aopacloud.superbi.model.dto.WorkspaceDTO;
import net.aopacloud.superbi.model.dto.WorkspaceUserResourceDTO;
import net.aopacloud.superbi.model.vo.WorkspaceUserResourceVO;
import net.aopacloud.superbi.model.vo.WorkspaceVO;
import net.aopacloud.superbi.service.WorkspaceService;
import net.aopacloud.superbi.service.WorkspaceUserResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Workspace
 * @author: hudong
 * @date: 2023/8/4
 * @description:
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("workspace")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    private final WorkspaceUserResourceService workspaceUserResourceService;

    private final WorkspaceConverter converter;

    private final WorkspaceUserResourceConverter workspaceUserResourceConverter;

    /**
     * get specified id workspace
     * @param id workspace id
     * @return
     */
    @GetMapping("/{id}")
    public RestApiResponse<WorkspaceVO> findOne(@PathVariable Long id){

        WorkspaceDTO workspace = workspaceService.findOne(id);

        return RestApiResponse.success(converter.toVO(workspace));
    }


    /**
     * get workspace list
     * no pagination
     * @return
     */
    @GetMapping
    public RestApiResponse<List<WorkspaceVO>> listAll() {

        List<WorkspaceDTO> workspaceDTOS =  workspaceService.listAll();

        return RestApiResponse.success(converter.toVOList(workspaceDTOS));
    }

    /**
     * get workspace list than I can manage
     * no pagination
     * @return
     */
    @GetMapping("/canManageList")
    public RestApiResponse<List<WorkspaceVO>> listCanManage() {

        List<WorkspaceDTO> workspaceDTOS =  workspaceService.listCanManage();

        return RestApiResponse.success(converter.toVOList(workspaceDTOS));
    }

    /**
     * get workspace list that belong to me
     * no pagination
     * @return
     */
    @GetMapping("/belongMe")
    public RestApiResponse<List<WorkspaceVO>> listAllBelongMe() {
        List<WorkspaceDTO> workspaceDTOS =  workspaceService.listBelongMe();
        return RestApiResponse.success(converter.toVOList(workspaceDTOS));
    }

    /**
     * create workspace
     * @param workspaceVO
     * @return
     */
    @PostMapping
    public RestApiResponse<WorkspaceVO> add(@RequestBody WorkspaceVO workspaceVO){

        WorkspaceDTO dto = workspaceService.save(converter.toDTO(workspaceVO));

        return RestApiResponse.success(converter.toVO(dto));
    }

    /**
     * update workspace
     * @param workspaceVO
     * @param id workspace id
     * @return
     */
    @PutMapping("/{id}")
    public RestApiResponse<WorkspaceVO> update(@RequestBody WorkspaceVO workspaceVO, @PathVariable Long id){

        workspaceVO.setId(id);
        WorkspaceDTO dto = workspaceService.update(converter.toDTO(workspaceVO));
        return RestApiResponse.success(converter.toVO(dto));
    }


    /**
     * delete workspace
     * @param id workspace id
     * @return
     */
    @DeleteMapping("/{id}")
    public RestApiResponse<WorkspaceVO> delete(@PathVariable Long id){
        WorkspaceDTO dto = workspaceService.delete(id);
        return RestApiResponse.success(converter.toVO(dto));
    }


    /**
     * get workspace user resource
     * @param id
     * @return
     */
    @GetMapping("/{id}/userResource")
    public RestApiResponse<WorkspaceUserResourceVO> getWorkspaceUserResource(@PathVariable Long id) {
        WorkspaceUserResourceDTO workspaceUserResourceDTO = workspaceUserResourceService.get(id);
        return RestApiResponse.success(workspaceUserResourceConverter.toVO(workspaceUserResourceDTO));
    }


    @PutMapping("/sortBy")
    public RestApiResponse<List<WorkspaceVO>> updateSort(@RequestBody List<WorkspaceVO> workspaceVOS) {
        List<WorkspaceDTO> workspaceDTOS = workspaceService.updateSort(converter.toDTOList(workspaceVOS));
        return RestApiResponse.success(converter.toVOList(workspaceDTOS));
    }
}
