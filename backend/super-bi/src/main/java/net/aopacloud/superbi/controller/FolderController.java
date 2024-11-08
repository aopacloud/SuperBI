package net.aopacloud.superbi.controller;

import lombok.RequiredArgsConstructor;
import net.aopacloud.superbi.common.core.web.domain.RestApiResponse;
import net.aopacloud.superbi.model.converter.FolderConverter;
import net.aopacloud.superbi.model.converter.FolderResourceRelationshipConverter;
import net.aopacloud.superbi.model.dto.FolderDTO;
import net.aopacloud.superbi.model.dto.FolderResourceRelationshipDTO;
import net.aopacloud.superbi.model.query.FolderQuery;
import net.aopacloud.superbi.model.vo.FolderNode;
import net.aopacloud.superbi.model.vo.FolderResourceRelationshipVO;
import net.aopacloud.superbi.model.vo.FolderVO;
import net.aopacloud.superbi.service.FolderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Folder
 *
 * @Author shinnie
 * @Description
 * @Date 11:33 2023/8/9
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("folder")
public class FolderController {

    private final FolderService folderService;

    private final FolderConverter converter;

    private final FolderResourceRelationshipConverter relationshipConverter;

    /**
     * create folder
     *
     * @param folderVO
     * @return
     */
    @PostMapping
    public RestApiResponse<FolderVO> newFolder(@RequestBody FolderVO folderVO) {
        folderService.save(converter.toDTO(folderVO));
        return RestApiResponse.success(folderVO);
    }

    /**
     * update folder
     *
     * @param folderVO
     * @param id       folder id
     * @return
     */
    @PutMapping("/{id}")
    public RestApiResponse<FolderVO> updateFolder(@RequestBody FolderVO folderVO, @PathVariable Long id) {
        folderVO.setId(id);
        folderService.update(converter.toDTO(folderVO));
        return RestApiResponse.success(folderVO);
    }

    /**
     * update folderList
     *
     * @param folderVOList
     * @return
     */
    @PutMapping("/list")
    public RestApiResponse<List<FolderVO>> updateFolderList(@RequestBody List<FolderVO> folderVOList) {
        folderService.updateList(converter.toDTOList(folderVOList));
        return RestApiResponse.success(folderVOList);
    }

    /**
     * delete folder
     *
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public RestApiResponse<FolderVO> deleteFolder(@PathVariable Long id) {
        FolderDTO folder = folderService.delete(id);
        return RestApiResponse.success(converter.toVO(folder));
    }

    /**
     * get folder tree
     *
     * @param folderQuery
     * @return
     */
    @GetMapping("/tree")
    public RestApiResponse<FolderNode> findFolderTree(FolderQuery folderQuery) {
        FolderNode folderNode = folderService.findFolderTree(folderQuery);
        return RestApiResponse.success(folderNode);
    }

    /**
     * get folder tree without count
     *
     * @param folderQuery
     * @return
     */
    @GetMapping("/treeWithoutCount")
    public RestApiResponse<FolderNode> findFolderTreeWithoutCount(FolderQuery folderQuery) {
        FolderNode folderNode = folderService.findFolderTreeWithoutCount(folderQuery);
        return RestApiResponse.success(folderNode);
    }

    /**
     * move resource relation
     *
     * @param folderResourceRel
     * @return
     */
    @PostMapping("/moveResource")
    public RestApiResponse<FolderResourceRelationshipVO> moveResource(@RequestBody FolderResourceRelationshipVO folderResourceRel) {
        FolderResourceRelationshipDTO folderResourceRelationshipDTO = folderService.moveResource(relationshipConverter.toDTO(folderResourceRel));
        return RestApiResponse.success(relationshipConverter.toVO(folderResourceRelationshipDTO));
    }

}
