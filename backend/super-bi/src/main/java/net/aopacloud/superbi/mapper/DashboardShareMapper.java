package net.aopacloud.superbi.mapper;

import net.aopacloud.superbi.enums.PermissionEnum;
import net.aopacloud.superbi.model.dto.DashboardShareDTO;
import net.aopacloud.superbi.model.entity.DashboardShare;
import net.aopacloud.superbi.model.query.DashboardShareBatchQuery;
import net.aopacloud.superbi.model.query.DashboardShareQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DashboardShareMapper {

    int insert(DashboardShare dashboardShare);

    List<DashboardShareDTO> searchUsers(DashboardShareQuery query);

    List<DashboardShareDTO> searchRole(DashboardShareQuery query);

    void update(DashboardShare dashboardShare);

    void deleteById(Long id);

    DashboardShare selectById(Long id);

    List<Long> selectIdsByUsername(String username);

    List<Long> selectIdsByUsernameWithWrite(String username);

    void deleteByDashboard(Long dashboardId);

    List<DashboardShareDTO> selectByDashboardAndUsername(@Param("dashboardId") Long dashboardId, @Param("username") String username);
    List<DashboardShareDTO> selectByWorkspaceAndUsername(@Param("workspaceId") Long workspaceId, @Param("username") String username);

    List<DashboardShareDTO> selectWriteShare(@Param("workspaceId") Long workspaceId, @Param("username") String username);

    List<DashboardShareDTO> selectByUsernameAndWorkspaceIds(@Param("username") String username, @Param("workspaceIds") List<Long> workspaceIds);

    List<DashboardShareDTO> selectByRole(@Param("roleId") Long roleId);

    List<DashboardShareDTO> selectByDashboard(@Param("dashboardId") Long dashboardId);

    List<DashboardShareDTO> search(DashboardShareBatchQuery query);

    List<DashboardShareDTO> selectByPermission(@Param("workspaceId") Long workspaceId, @Param("username") String username, @Param("permission") PermissionEnum permission);

    void deleteByUsername(String username);
}