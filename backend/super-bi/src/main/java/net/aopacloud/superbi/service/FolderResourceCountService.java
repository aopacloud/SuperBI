package net.aopacloud.superbi.service;

import net.aopacloud.superbi.model.vo.FolderNode;

public interface FolderResourceCountService {
    Integer countResourceByFolder(FolderNode folder, String userName);

    Integer countFavoriteDashboardByUser(String username, Long workspaceId);

    Integer countFavoriteDatasetByUser(String username, Long workspaceId);

    Integer countCreateDashboard(String username, Long workspaceId);

    Integer countCreateDataset(String username, Long workspaceId);

    Integer countShareToMeDashboard(String username, Long workspaceId);

    Integer countHasPermissionDataset(String username, Long workspaceId);
}
