package net.aopacloud.superbi.controller;

import net.aopacloud.superbi.auth.SuperAdminPermission;
import net.aopacloud.superbi.common.core.web.domain.RestApiResponse;
import net.aopacloud.superbi.model.converter.SysRoleResourceConverter;
import net.aopacloud.superbi.model.dto.SysRoleResourceDTO;
import net.aopacloud.superbi.model.vo.SysRoleResourceVO;
import net.aopacloud.superbi.service.SysRoleResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sysRoleResource")
public class SysRoleResourceController {

    private final SysRoleResourceService sysRoleResourceService;

    private final SysRoleResourceConverter converter;
    @PostMapping
    @SuperAdminPermission
    public RestApiResponse<SysRoleResourceVO> saveOrUpdate(@RequestBody SysRoleResourceVO sysRoleResourceVO) {
        SysRoleResourceDTO sysRoleResourceDTO = sysRoleResourceService.saveOrUpdate(converter.toDTO(sysRoleResourceVO));
        return RestApiResponse.success(converter.toVO(sysRoleResourceDTO));
    }

    @GetMapping
    public RestApiResponse<SysRoleResourceVO> findByRole(Long roleId) {
        SysRoleResourceDTO sysRoleResourceDTO = sysRoleResourceService.findByRole(roleId);
        return RestApiResponse.success(converter.toVO(sysRoleResourceDTO));
    }

}
