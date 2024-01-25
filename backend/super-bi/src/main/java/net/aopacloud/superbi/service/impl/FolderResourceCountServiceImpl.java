package net.aopacloud.superbi.service.impl;

import net.aopacloud.superbi.constant.BiConsist;
import net.aopacloud.superbi.mapper.FolderResourceCountMapper;
import net.aopacloud.superbi.model.vo.FolderNode;
import net.aopacloud.superbi.service.FolderResourceCountService;
import net.aopacloud.superbi.service.SysUserService;
import net.aopacloud.superbi.service.WorkspaceUserResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * @author: hudong
 * @date: 2023/10/27
 * @description:
 */
@Service
@RequiredArgsConstructor
public class FolderResourceCountServiceImpl implements FolderResourceCountService {

    private final FolderResourceCountMapper resourceCountMapper;

    private final WorkspaceUserResourceService workspaceUserResourceService;

    private final SysUserService sysUserService;

    @Override
    public Integer countResourceByFolder(FolderNode folder, String userName) {
        return resourceCountMapper.countResourceByFolder(folder, userName);
    }

    @Override
    public Integer countFavoriteDashboardByUser(String username, Long workspaceId) {
        return resourceCountMapper.countFavoriteDashboardByUser(username, workspaceId);
    }

    @Override
    public Integer countFavoriteDatasetByUser(String username, Long workspaceId) {
        return resourceCountMapper.countFavoriteDatasetByUser(username, workspaceId);
    }

    @Override
    public Integer countCreateDashboard(String username, Long workspaceId) {
        return resourceCountMapper.countCreateDashboard(username, workspaceId);
    }

    @Override
    public Integer countCreateDataset(String username, Long workspaceId) {
        return resourceCountMapper.countCreateDataset(username, workspaceId);
    }

    @Override
    public Integer countShareToMeDashboard(String username, Long workspaceId) {
        return resourceCountMapper.countShareToMeDashboard(username, workspaceId);
    }

    @Override
    public Integer countHasPermissionDataset(String username, Long workspaceId) {
        Set<String> resourceCodes = workspaceUserResourceService.getResourceCodes(workspaceId);
        boolean allPermission = resourceCodes.contains(BiConsist.ALL_WORKSPACE_ANALYSIS_CODE) || sysUserService.isSuperAdmin(username);

        return resourceCountMapper.countHasPermissionDataset(username, workspaceId, allPermission);
    }
}
