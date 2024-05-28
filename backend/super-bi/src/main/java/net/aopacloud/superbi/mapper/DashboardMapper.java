package net.aopacloud.superbi.mapper;

import net.aopacloud.superbi.enums.StatusEnum;
import net.aopacloud.superbi.enums.Visibility;
import net.aopacloud.superbi.model.dto.DashboardDTO;
import net.aopacloud.superbi.model.entity.Dashboard;
import net.aopacloud.superbi.model.query.ConditionQuery;
import net.aopacloud.superbi.model.query.DashboardQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public interface DashboardMapper {

    List<DashboardDTO> search(DashboardQuery dashboardQuery);

    DashboardDTO selectById(@Param("id") Long id);

    void save(Dashboard dashboard);

    void delete(@Param("id") Long id);

    void update(Dashboard dashboard);

    void updateLastEditVersion(@Param("dashboardId") Long dashboardId, @Param("version") Long version);

    void updateStatus(@Param("id") Long id, @Param("status") StatusEnum status);

    void updateVersion(@Param("dashboardId") Long dashboardId, @Param("version") Integer version);

    List<DashboardDTO> selectByReport(Long reportId);

    List<DashboardDTO> findAuthorized(ConditionQuery condition);

    List<DashboardDTO> findFavorite(ConditionQuery condition);

    List<DashboardDTO> findCreate(ConditionQuery condition);

    List<DashboardDTO> findUnclassified(ConditionQuery condition);

    List<DashboardDTO> findAll(ConditionQuery condition);

    List<DashboardDTO> findRoot(ConditionQuery condition);

    List<DashboardDTO> findNormal(ConditionQuery condition);

    Integer countByUsername(@Param("username") String username);

    void updateCreator(@Param("fromUsername") String fromUsername, @Param("toUsername") String toUsername);

    List<DashboardDTO> selectByWorkspace(Long workspaceId);

    List<DashboardDTO> selectByIdsAndCreator(@Param("dashboardIds") Set<Long> dashboardIds, @Param("username") String username);

    List<DashboardDTO> selectOnlineDashboard(@Param("workspaceId") Long workspaceId);

    List<Long> selectIdByWorkspaceAndCreator(@Param("workspaceId") Long workspaceId, @Param("username") String username);

    void updateRefreshInterval(@Param("id") Long id, @Param("refreshIntervalSeconds") Integer refreshIntervalSeconds);

    void updateVisibility(@Param("id") Long id,@Param("visibility") Visibility visibility);
}
