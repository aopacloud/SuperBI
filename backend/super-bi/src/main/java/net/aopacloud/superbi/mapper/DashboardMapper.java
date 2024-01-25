package net.aopacloud.superbi.mapper;

import net.aopacloud.superbi.enums.StatusEnum;
import net.aopacloud.superbi.model.dto.DashboardDTO;
import net.aopacloud.superbi.model.entity.Dashboard;
import net.aopacloud.superbi.model.query.ConditionQuery;
import net.aopacloud.superbi.model.query.DashboardQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DashboardMapper {

    List<DashboardDTO> search(DashboardQuery dashboardQuery);

    DashboardDTO selectById(@Param("id") Long id);

    void save(Dashboard dashboard);

    void delete(@Param("id") Long id);

    void update(Dashboard dashboard);

    void updateLastEditVersion(@Param("dashboardId") Long dashboardId, @Param("version") Long version);

    void updateStatus(@Param("id") Long id, @Param("status") StatusEnum status);

    void updateVersion(@Param("dashboardId") Long dashboardId,@Param("version") Integer version);

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
}
