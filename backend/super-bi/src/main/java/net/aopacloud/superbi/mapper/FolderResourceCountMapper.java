package net.aopacloud.superbi.mapper;

import net.aopacloud.superbi.model.vo.FolderNode;
import org.apache.ibatis.annotations.Param;

public interface FolderResourceCountMapper {
    Integer countResourceByFolder(@Param("folder") FolderNode folder, @Param("username") String username);

    Integer countFavoriteDashboardByUser(@Param("username") String username, @Param("workspaceId") Long workspaceId);

    Integer countFavoriteDatasetByUser(@Param("username") String username, @Param("workspaceId") Long workspaceId);

    Integer countCreateDashboard(@Param("username") String username, @Param("workspaceId") Long workspaceId);

    Integer countCreateDataset(@Param("username") String username, @Param("workspaceId") Long workspaceId);

    Integer countShareToMeDashboard(@Param("username") String username, @Param("workspaceId") Long workspaceId);

    Integer countHasPermissionDataset(@Param("username") String username, @Param("workspaceId") Long workspaceId, @Param("allPermission") boolean allPermission);
}
