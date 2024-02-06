package net.aopacloud.superbi.controller;

import lombok.RequiredArgsConstructor;
import net.aopacloud.superbi.auth.SuperAdminPermission;
import net.aopacloud.superbi.common.core.web.domain.RestApiResponse;
import net.aopacloud.superbi.model.converter.SysMenuConverter;
import net.aopacloud.superbi.model.converter.SysRoleMenuConverter;
import net.aopacloud.superbi.model.dto.SysMenuDTO;
import net.aopacloud.superbi.model.vo.SysMenuVO;
import net.aopacloud.superbi.model.vo.SysRoleMenuVO;
import net.aopacloud.superbi.service.SysRoleMenuService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sysRoleMenu")
public class SysRoleMenuController {

    private final SysRoleMenuService sysRoleMenuService;

    private final SysRoleMenuConverter converter;

    private final SysMenuConverter sysMenuConverter;

    /**
     * add or update menu to role
     *
     * @return
     */
    @PostMapping
    @SuperAdminPermission
    public RestApiResponse saveOrUpdate(@RequestBody List<SysRoleMenuVO> sysRoleMenuVOS) {
        sysRoleMenuService.saveOrUpdate(converter.toDTOList(sysRoleMenuVOS));
        return RestApiResponse.success();
    }

    /**
     * find menu list by role id
     *
     * @param roleId
     * @return
     */
    @GetMapping
    public RestApiResponse<List<SysMenuVO>> findByRoleId(Long roleId) {
        List<SysMenuDTO> sysMenuDTOS = sysRoleMenuService.findByRoleId(roleId);
        return RestApiResponse.success(sysMenuConverter.toVOList(sysMenuDTOS));
    }
}
