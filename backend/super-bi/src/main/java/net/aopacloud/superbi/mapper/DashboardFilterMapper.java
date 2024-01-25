package net.aopacloud.superbi.mapper;

import net.aopacloud.superbi.model.entity.DashboardFilter;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DashboardFilterMapper {

    List<DashboardFilter> selectByComponent(@Param("chartId") Integer chartId);

    void deleteByChart(@Param("componentId") Integer componentId);

    int insert(DashboardFilter dashboardFilter);

}