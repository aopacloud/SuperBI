package net.aopacloud.superbi.service;

import net.aopacloud.superbi.enums.PositionEnum;
import net.aopacloud.superbi.model.dto.FolderDTO;
import net.aopacloud.superbi.model.dto.FolderResourceRelationshipDTO;
import net.aopacloud.superbi.model.dto.FullFolderDTO;
import net.aopacloud.superbi.model.query.FolderQuery;
import net.aopacloud.superbi.model.vo.FolderNode;

/**
 * @Author shinnie
 * @Description
 * @Date 17:28 2023/8/10
 */
public interface FolderService {

    FolderDTO save(FolderDTO folderDTO);

    FolderDTO update(FolderDTO folderDTO);

    FolderDTO delete(Long id);

    FolderNode findFolderTree(FolderQuery folderQuery);

    FolderNode findFolderTreeWithoutCount(FolderQuery folderQuery);

    FolderResourceRelationshipDTO moveResource(FolderResourceRelationshipDTO folderResourceRel);

    FullFolderDTO findByTarget(Long targetId, PositionEnum position);
    FullFolderDTO findPersonalByTarget(Long targetId, PositionEnum position, String username);

    void addFolderResourceRel(Long folderId, Long targetId, PositionEnum positionEnum);

    FolderNode findTreeByFolder(Long folderId);

}
