package net.aopacloud.superbi.mapper;

import net.aopacloud.superbi.model.entity.WorkspaceConfig;

/**
 * @author: hudong
 * @date: 2023/8/4
 * @description:
 */
public interface WorkspaceConfigMapper {
    WorkspaceConfig selectOneByWorkspaceId(Long workspaceId);

    void insert(WorkspaceConfig workspaceConfig);

    void update(WorkspaceConfig workspaceConfig);

    void deleteById(Long id);
}
