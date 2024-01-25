package net.aopacloud.superbi.controller;

import net.aopacloud.superbi.common.core.web.domain.RestApiResponse;
import net.aopacloud.superbi.common.core.web.page.PageVO;
import net.aopacloud.superbi.auth.ApiPermission;
import net.aopacloud.superbi.model.converter.DashboardComponentConverter;
import net.aopacloud.superbi.model.converter.DashboardConverter;
import net.aopacloud.superbi.model.dto.DashboardDTO;
import net.aopacloud.superbi.model.dto.SysUserDTO;
import net.aopacloud.superbi.model.query.DashboardQuery;
import net.aopacloud.superbi.model.vo.DashboardVO;
import net.aopacloud.superbi.service.DashboardService;
import net.aopacloud.superbi.service.SysUserService;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.assertj.core.util.Strings;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Dashboard
 * @author: hudong
 * @date: 2022/3/29
 * @description:
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    private final SysUserService sysUserService;

    private final DashboardConverter dashboardConverter;

    private final DashboardComponentConverter dashboardComponentConverter;


    /**
     * get dashboard list
     * @param dashboardQuery
     * @return
     */
    @GetMapping
    public RestApiResponse<PageVO<DashboardVO>> search(DashboardQuery dashboardQuery) {

        if (!Strings.isNullOrEmpty(dashboardQuery.getKeyword())) {
            List<SysUserDTO> sysUsers = sysUserService.filter(dashboardQuery.getKeyword());
            if (!sysUsers.isEmpty()) {
                List<String> usernames = sysUsers.stream().map(SysUserDTO::getUsername).collect(Collectors.toList());
                dashboardQuery.setSearchUsers(usernames);
            }
        }

        List<DashboardDTO> dashboardDTOS = dashboardService.search(dashboardQuery);

        PageInfo pageInfo = new PageInfo(dashboardDTOS);
        PageVO pageVO = new PageVO(dashboardConverter.toVOList(dashboardDTOS), pageInfo.getTotal());

        return RestApiResponse.success(pageVO);
    }

    /**
     * get specified id dashboard
     * @param id dashboard id
     * @return return latest published version
     */
    @GetMapping("/{id}")
    public RestApiResponse<DashboardVO> findOne(@PathVariable Long id) {
        DashboardDTO dashboardDTO = dashboardService.findOne(id);
        return RestApiResponse.success(dashboardConverter.toVO(dashboardDTO));
    }

    /**
     * get specified id and version dashboard
     * @param id dashboard id
     * @param version dashboard version
     * @return return specified version dashbaord
     */
    @GetMapping("/{id}/version/{version}")
    public RestApiResponse<DashboardVO> findOneByVersion(@PathVariable Long id, @PathVariable int version) {
        DashboardDTO dashboardDTO = dashboardService.findOne(id, version);
        return RestApiResponse.success(dashboardConverter.toVO(dashboardDTO));
    }

    /**
     * get latest edit version dashboard
     * @param id dashboard id
     * @return return latest edit version dashboard
     */
    @GetMapping("/{id}/version/lastEdit")
    public RestApiResponse<DashboardVO> findLastEditVersion(@PathVariable Long id) {
        DashboardDTO dashboardDTO = dashboardService.findLastEditVersion(id);
        return RestApiResponse.success(dashboardConverter.toVO(dashboardDTO));
    }

/**
     * get dashboard by report id
     * @param reportId report id
     * @return return dashboard list
     */
    @GetMapping("/byReport")
    public RestApiResponse<List<DashboardVO>>  findByReport(Long reportId) {
        List<DashboardDTO> dashboardDTOS = dashboardService.findByReport(reportId);
        return RestApiResponse.success(dashboardConverter.toVOList(dashboardDTOS));
    }
    /**
     * save dashboard
     * @param dashboardVO
     * @return return saved dashboard
     */
    @PostMapping
    @ApiPermission("DASHBOARD:VIEW:CREATE")
    public RestApiResponse<DashboardVO> save(@RequestBody DashboardVO dashboardVO) {
        DashboardDTO newDashboard = dashboardService.save(dashboardConverter.toDTO(dashboardVO));
        return RestApiResponse.success(dashboardConverter.toVO(newDashboard));
    }

    /**
     * update dashboard
     * @param dashboardVO
     * @param id dashboard id
     * @return return updated dashboard
     */
    @PutMapping("/{id}")
    public RestApiResponse<DashboardVO> update(@RequestBody DashboardVO dashboardVO, @PathVariable Long id) {
        DashboardDTO newDashboard = dashboardService.update(dashboardConverter.toDTO(dashboardVO), id);
        return RestApiResponse.success(dashboardConverter.toVO(newDashboard));
    }

    /**
     * delete dashboard
     * soft delete
     * @param id dashboard id
     * @return
     */
    @DeleteMapping("/{id}")
    public RestApiResponse<String> delete(@PathVariable Long id) {
        dashboardService.delete(id);
        return RestApiResponse.success("ok");
    }

//
//    @PostMapping("/{id}/chartFilter")
//    public RestApiResponse saveOrUpdateChartFilter(@RequestBody DashboardComponentVO componentVO, @PathVariable Long id) {
//        dashboardService.saveOrUpdateFilter(dashboardComponentConverter.toDTO(componentVO));
//        return RestApiResponse.success(componentVO);
//    }

    /**
     * offline specified dashboard
     * @param id specified dashboard id
     * @return
     */
    @PostMapping("/{id}/offline")
    public RestApiResponse<String> offline(@PathVariable Long id) {
        dashboardService.offline(id);
        return RestApiResponse.success("ok");
    }

    /**
     * online specified dashboard
     * @param id specified dashboard id
     * @return
     */
    @PostMapping("/{id}/online")
    public RestApiResponse<String> online(@PathVariable Long id) {
        dashboardService.online(id);
        return RestApiResponse.success("ok");
    }

    /**
     * publish specified dashboard
     * @param version specified dashboard version
     * @param id specified dashboard id
     * @return
     */
    @PostMapping("/{id}/publish")
    public RestApiResponse<DashboardVO> publish(Integer version, @PathVariable Long id) {
        DashboardDTO dashboardDTO = dashboardService.publish(id, version);
        return RestApiResponse.success(dashboardConverter.toVO(dashboardDTO));
    }

}
