package net.aopacloud.superbi.service;

import net.aopacloud.superbi.model.dto.DashboardDTO;
import net.aopacloud.superbi.model.dto.ReportDTO;
import net.aopacloud.superbi.model.query.ReportQuery;

import java.util.List;

/**
 * @Author shinnie
 * @Description
 * @Date 10:55 2023/8/29
 */
public interface ReportService {

    ReportDTO findOne(Long id);

    List<DashboardDTO> findDashboard(Long id);

    List<ReportDTO> search(ReportQuery query);

    ReportDTO saveOrUpdate(ReportDTO reportDTO);

    ReportDTO saveAs(ReportDTO reportDTO);

    void deleteById(Long id);

    Integer countByUsername(String username);

    void handOver(String fromUsername, String toUsername);

}
