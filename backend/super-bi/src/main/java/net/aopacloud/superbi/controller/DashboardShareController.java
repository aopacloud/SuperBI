package net.aopacloud.superbi.controller;

import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import net.aopacloud.superbi.common.core.utils.PageUtils;
import net.aopacloud.superbi.common.core.web.domain.RestApiResponse;
import net.aopacloud.superbi.common.core.web.page.PageVO;
import net.aopacloud.superbi.model.converter.DashboardShareConverter;
import net.aopacloud.superbi.model.dto.DashboardShareDTO;
import net.aopacloud.superbi.model.dto.SysUserDTO;
import net.aopacloud.superbi.model.query.DashboardShareQuery;
import net.aopacloud.superbi.model.query.DashboardShareSaveVO;
import net.aopacloud.superbi.model.vo.DashboardShareVO;
import net.aopacloud.superbi.service.DashboardShareService;
import net.aopacloud.superbi.service.SysUserService;
import org.assertj.core.util.Strings;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Dashboard share
 *
 * @author: hudong
 * @date: 2022/10/20
 * @description:
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("dashboardShare")
public class DashboardShareController {

    private final DashboardShareService dashboardShareService;

    private final SysUserService sysUserService;

    private final DashboardShareConverter converter;

    /**
     * share dashboard
     *
     * @param dashboardShareSaveVO
     * @return
     */
    @PostMapping()
    public RestApiResponse<List<DashboardShareVO>> share(@RequestBody DashboardShareSaveVO dashboardShareSaveVO) {

        List<DashboardShareDTO> savedDashboardShare = dashboardShareService.share(dashboardShareSaveVO);

        return RestApiResponse.success(converter.toVOList(savedDashboardShare));
    }

    /**
     * update dashboard share
     *
     * @param dashboardShareVO
     * @param id               share id
     * @return
     */
    @PutMapping("/{id}")
    public RestApiResponse<DashboardShareVO> update(@RequestBody DashboardShareVO dashboardShareVO, @PathVariable Long id) {
        dashboardShareVO.setId(id);
        DashboardShareDTO shareDTO = dashboardShareService.update(converter.toDTO(dashboardShareVO));
        return RestApiResponse.success(converter.toVO(shareDTO));
    }


    /**
     * delete dashboard share
     *
     * @param id specified share id
     * @return
     */
    @DeleteMapping("/{id}")
    public RestApiResponse<DashboardShareVO> delete(@PathVariable Long id) {

        DashboardShareDTO dashboardShareDTO = dashboardShareService.delete(id);
        return RestApiResponse.success(converter.toVO(dashboardShareDTO));
    }


    /**
     * get dashboard share user list
     *
     * @param query
     * @return return dashboard share user list
     */
    @GetMapping("/users")
    public RestApiResponse<PageVO<DashboardShareVO>> listUsers(DashboardShareQuery query) {


        if (!Strings.isNullOrEmpty(query.getKeyword())) {
            List<SysUserDTO> searchUsers = sysUserService.filter(query.getKeyword());
            query.setSearchUsers(searchUsers.stream().map(SysUserDTO::getUsername).collect(Collectors.toList()));
        }
        PageUtils.startPage();
        List<DashboardShareDTO> shareDTOS = dashboardShareService.searchUsers(query);
        PageInfo pageInfo = new PageInfo(shareDTOS);
        PageVO pageVO = new PageVO(converter.toVOList(shareDTOS), pageInfo.getTotal());
        return RestApiResponse.success(pageVO);

    }

    /**
     * get dashboard share role list
     *
     * @param query
     * @return return dashboard share role list
     */
    @GetMapping("/roles")
    public RestApiResponse<PageVO<DashboardShareVO>> listRoles(DashboardShareQuery query) {
        if (!Strings.isNullOrEmpty(query.getKeyword())) {
            List<SysUserDTO> searchUsers = sysUserService.filter(query.getKeyword());
            query.setSearchUsers(searchUsers.stream().map(SysUserDTO::getUsername).collect(Collectors.toList()));
        }

        PageUtils.startPage();
        List<DashboardShareDTO> shareDTOS = dashboardShareService.searchRoles(query);
        PageInfo pageInfo = new PageInfo(shareDTOS);
        PageVO pageVO = new PageVO(converter.toVOList(shareDTOS), pageInfo.getTotal());
        return RestApiResponse.success(pageVO);
    }

    @DeleteMapping("/clear")
    public RestApiResponse clear(Long dashboardId) {
        dashboardShareService.clearByDashboard(dashboardId);
        return RestApiResponse.success();
    }
}
