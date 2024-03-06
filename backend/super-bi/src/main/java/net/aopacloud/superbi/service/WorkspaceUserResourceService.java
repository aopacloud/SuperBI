package net.aopacloud.superbi.service;

import net.aopacloud.superbi.model.dto.WorkspaceUserResourceDTO;

import java.util.List;
import java.util.Set;

public interface WorkspaceUserResourceService {

    WorkspaceUserResourceDTO get(Long workspaceId);

    Set<String> getResourceCodes(Long workspaceId);

    List<Long> getCanMangeDatasetIds(Long workspaceId, String username);
    List<Long> getCanMangeDashboardIds(Long workspaceId, String username);
}
