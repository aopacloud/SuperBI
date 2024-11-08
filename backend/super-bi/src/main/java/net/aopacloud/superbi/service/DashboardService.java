package net.aopacloud.superbi.service;

import net.aopacloud.superbi.common.core.web.page.PageVO;
import net.aopacloud.superbi.model.dto.DashboardComponentDTO;
import net.aopacloud.superbi.model.dto.DashboardDTO;
import net.aopacloud.superbi.model.query.DashboardQuery;
import net.aopacloud.superbi.model.query.RecycleQuery;
import net.aopacloud.superbi.model.uo.DashboardVisibilityUO;
import net.aopacloud.superbi.model.vo.DashboardVO;
import net.aopacloud.superbi.model.vo.RecycleVO;

import java.util.List;
import java.util.Set;

/**
 * @Author shinnie
 * @Description
 * @Date 15:54 2023/8/30
 */
public interface DashboardService {

    PageVO<DashboardVO> search(DashboardQuery dashboardQuery);

    PageVO<RecycleVO> searchByRecycle(RecycleQuery recycleQuery);

    DashboardDTO findOne(Long id);

    DashboardDTO findOne(Long id, Integer version);

    DashboardDTO findLastEditVersion(Long id);

    DashboardDTO save(DashboardDTO dashboardDTO);

    DashboardDTO update(DashboardDTO dashboardDTO, Long id);

    void recycleDelete(Long id);

    void delete(Long id);

    void restore(Long id);

    void offline(Long id);

    void online(Long id);

    DashboardDTO publish(Long id, Integer version);

    List<DashboardDTO> findByReport(Long reportId);

    Integer countByUsername(String username);

    void handOver(String fromUsername, String toUsername);

    void handOverById(Long id, String fromUsername, String toUsername);

    List<DashboardDTO> findDashboardCanShare(Long workspaceId, String username);

    List<DashboardDTO> findDashboardCanCopy(Long workspaceId, String username);

    Set<Long> findDatasetIdsByDashboardId(Long dashboardId);

    DashboardDTO updateRefreshInterval(Long id, Integer refreshIntervalSeconds);

    DashboardDTO updateVisibility(DashboardVisibilityUO dashboardVisibilityUO);
}
