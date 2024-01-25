package net.aopacloud.superbi.service;

import net.aopacloud.superbi.model.dto.WorkspaceUserResourceDTO;

import java.util.Set;

public interface WorkspaceUserResourceService {

    WorkspaceUserResourceDTO get(Long workspaceId);

    Set<String> getResourceCodes(Long workspaceId);
}
