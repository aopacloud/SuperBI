package net.aopacloud.superbi.service;

import net.aopacloud.superbi.common.core.web.page.PageVO;
import net.aopacloud.superbi.model.dto.DashboardDTO;
import net.aopacloud.superbi.model.dto.ReportDTO;
import net.aopacloud.superbi.model.query.RecycleQuery;
import net.aopacloud.superbi.model.query.ReportQuery;
import net.aopacloud.superbi.model.vo.RecycleVO;
import net.aopacloud.superbi.model.vo.ReportVO;

import java.util.List;

/**
 * @Author shinnie
 * @Description
 * @Date 10:55 2023/8/29
 */
public interface ReportService {

    ReportDTO findOne(Long id);

    ReportDTO selectByIdRecycle(Long id);

    List<DashboardDTO> findDashboard(Long id);

    PageVO<ReportVO> search(ReportQuery query);

    PageVO<RecycleVO> searchByRecycle(RecycleQuery recycleQuery);

    ReportDTO saveOrUpdate(ReportDTO reportDTO);

    ReportDTO saveAs(ReportDTO reportDTO);

    void deleteById(Long id);

    void recycleDelete(Long id);

    void restore(Long id);

    Integer countByUsername(String username);

    Boolean isActive(Long id);

    void handOver(String fromUsername, String toUsername);

    void handOverById(Long id, String fromUsername, String toUsername);

    List<ReportDTO> findByDataset(Long datasetId);

    List<ReportDTO> selectByDatasetRecycle(Long datasetId);

}
