package net.aopacloud.superbi.mapper;

import net.aopacloud.superbi.model.dto.FolderResourceRelationshipDTO;
import net.aopacloud.superbi.model.entity.Folder;
import net.aopacloud.superbi.model.query.FolderQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FolderMapper {

    void save(Folder folder);

    void update(Folder folder);

    Folder select(Folder folder);

    Folder selectById(@Param("id") Long id);

    void deleteById(@Param("id") Long id);

    List<Folder> queryFolder(@Param("folderQuery") FolderQuery folderQuery);

    List<Folder> selectChildren(@Param("id") Long id, @Param("folderQuery") FolderQuery folderQuery);

    Folder selectByTarget(@Param("targetId") Long targetId, @Param("position") String position);

    Folder selectPersonalByTarget(@Param("targetId") Long targetId, @Param("position") String position, @Param("username") String username);

    Folder selectByRelationship(FolderResourceRelationshipDTO relationship);

}
