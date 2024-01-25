package net.aopacloud.superbi.service;

import net.aopacloud.superbi.enums.PermissionEnum;
import net.aopacloud.superbi.model.dto.DashboardShareDTO;
import net.aopacloud.superbi.model.query.DashboardShareQuery;
import net.aopacloud.superbi.model.query.DashboardShareSaveVO;

import java.util.List;

/**
 * @Author shinnie
 * @Description
 * @Date 19:20 2023/8/30
 */
public interface DashboardShareService {

    List<DashboardShareDTO> share(DashboardShareSaveVO dashboardShareSaveVO);

    List<DashboardShareDTO> searchUsers(DashboardShareQuery query);

    List<DashboardShareDTO> searchRoles(DashboardShareQuery query);

    DashboardShareDTO update(DashboardShareDTO dashboardShareDTO);

    DashboardShareDTO delete(Long id);

    List<Long> findDashboardIdsByUsername(String username);

    void clearByDashboard(Long dashboardId);

    PermissionEnum findMixedPermission(Long dashboardId, String username);
}
