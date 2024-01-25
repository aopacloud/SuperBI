package net.aopacloud.superbi.mapper;

import net.aopacloud.superbi.model.dto.DashboardShareDTO;
import net.aopacloud.superbi.model.entity.DashboardShare;
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

    void deleteByDashboard(Long dashboardId);

    List<DashboardShareDTO> selectByDashboardAndUsername(@Param("dashboardId") Long dashboardId, @Param("username") String username);
}