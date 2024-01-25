package net.aopacloud.superbi.controller;

import net.aopacloud.superbi.common.core.web.domain.RestApiResponse;
import net.aopacloud.superbi.common.core.web.page.PageVO;
import net.aopacloud.superbi.auth.ApiPermission;
import net.aopacloud.superbi.model.converter.ReportConverter;
import net.aopacloud.superbi.model.dto.DashboardDTO;
import net.aopacloud.superbi.model.dto.ReportDTO;
import net.aopacloud.superbi.model.dto.SysUserDTO;
import net.aopacloud.superbi.model.query.ReportQuery;
import net.aopacloud.superbi.model.vo.ReportVO;
import net.aopacloud.superbi.service.ReportService;
import net.aopacloud.superbi.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Report
 * @Author shinnie
 * @Description
 * @Date 10:55 2023/8/29
 */
@RestController
@RequestMapping("report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    private final SysUserService sysUserService;

    private final ReportConverter reportConverter;

    /**
     * get specified id report
     * @param id report id
     * @return
     */
    @GetMapping("/{id}")
    public RestApiResponse<ReportVO> findOne(@PathVariable Long id) {
        ReportDTO reportDTO = reportService.findOne(id);
        return RestApiResponse.success(reportConverter.toVO(reportDTO));
    }

    @GetMapping("/{id}/dashboards")
    public RestApiResponse<List<DashboardDTO>> findDashboard(@PathVariable Long id) {
        List<DashboardDTO> dashboardDTOS = reportService.findDashboard(id);
        return RestApiResponse.success(dashboardDTOS);
    }

    /**
     * get report list
     * no pagination
     * @param query
     * @return
     */
    @GetMapping
    public RestApiResponse<PageVO<ReportVO>> search(ReportQuery query) {

        List<SysUserDTO> sysUsers = sysUserService.filter(query.getKeyword());
        if (!sysUsers.isEmpty()) {
            List<String> usernames = sysUsers.stream().map(SysUserDTO::getUsername).collect(Collectors.toList());
            query.setSearchUsers(usernames);
        }

        List<ReportDTO> reportDTOS = reportService.search(query);
        PageVO pageVO = new PageVO(reportConverter.toVOList(reportDTOS), reportDTOS.size());
        return RestApiResponse.success(pageVO);
    }

    /**
     * save report
     * @param reportVO
     * @return
     */
    @PostMapping
    @ApiPermission("REPORT:VIEW:CREATE")
    public RestApiResponse<ReportVO> save(@RequestBody ReportVO reportVO) {

        ReportDTO reportDTO = reportService.saveOrUpdate(reportConverter.toDTO(reportVO));

        return RestApiResponse.success(reportConverter.toVO(reportDTO));
    }

    /**
     * update report
     * @param reportVO
     * @return
     */
    @PutMapping("/{id}")
    public RestApiResponse<ReportVO> update(@RequestBody ReportVO reportVO, @PathVariable Long id) {
        reportVO.setId(id);
        ReportDTO reportDTO = reportService.saveOrUpdate(reportConverter.toDTO(reportVO));
        return RestApiResponse.success(reportConverter.toVO(reportDTO));
    }

    /**
     * save as report
     * @param reportVO
     * @return
     */
    @PostMapping("/saveAs")
    public RestApiResponse<ReportVO> saveAs(@RequestBody ReportVO reportVO) {

        ReportDTO reportDTO = reportService.saveAs(reportConverter.toDTO(reportVO));

        return RestApiResponse.success(reportConverter.toVO(reportDTO));
    }

    /**
     * delete report
     * @param id report id
     * @return
     */
    @DeleteMapping("/{id}")
    public RestApiResponse<String> delete(@PathVariable Long id) {

        reportService.deleteById(id);

        return RestApiResponse.success("ok");
    }

}
