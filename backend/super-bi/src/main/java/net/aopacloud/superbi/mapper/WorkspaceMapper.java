package net.aopacloud.superbi.mapper;

import net.aopacloud.superbi.model.entity.Workspace;

import java.util.List;

public interface WorkspaceMapper {

    List<Workspace> selectAllBelongMe(String me);

    Workspace selectOneById(Long id);

    Workspace selectOneByName(String name);

    List<Workspace> selectAll();

    void insert(Workspace workspace);

    void update(Workspace workspace);

    void deleteById(Long id);

    void updateSort(Workspace workspace);
}
