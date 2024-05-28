package net.aopacloud.superbi.mapper;

import net.aopacloud.superbi.model.dto.FolderResourceRelCountDTO;
import net.aopacloud.superbi.model.dto.FolderResourceRelationshipDTO;
import net.aopacloud.superbi.model.entity.Folder;
import net.aopacloud.superbi.model.entity.FolderResourceRel;

import java.util.List;

public interface FolderResourceRelMapper {

    int deleteById(Long id);

    int insert(FolderResourceRel row);

    FolderResourceRel selectById(Long id);

    FolderResourceRel selectByTargetId(Long targetId);

    List<FolderResourceRel> selectByFolders(List<Long> list);

    List<FolderResourceRelCountDTO> countByFolders(List<Long> list);

    int update(FolderResourceRel row);

    int countByFolder(Folder folder);

    void deleteByRelationship(FolderResourceRelationshipDTO relationship);

    void deleteByFolderId(Long id);
}