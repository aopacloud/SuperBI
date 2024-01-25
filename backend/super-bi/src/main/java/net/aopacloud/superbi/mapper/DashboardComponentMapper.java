package net.aopacloud.superbi.mapper;

import net.aopacloud.superbi.model.entity.DashboardComponent;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DashboardComponentMapper {

    DashboardComponent selectById(@Param("id") Integer id);

    List<DashboardComponent> selectByDashboardAndVersion(@Param("dashboardId") Long dashboardId,@Param("version") Integer version);

    int insert(DashboardComponent row);

    int update(DashboardComponent row);

    int deleteById(@Param("id") Integer id);

    List<String> selectReportNameByDashboard(@Param("dashboardId") Long dashboardId, @Param("version") Integer version);

    Integer countDashboardByReport(@Param("reportId") Long reportId);
}