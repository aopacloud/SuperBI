package net.aopacloud.superbi.controller;

import lombok.RequiredArgsConstructor;
import net.aopacloud.superbi.auth.SuperAdminPermission;
import net.aopacloud.superbi.common.core.web.domain.RestApiResponse;
import net.aopacloud.superbi.model.converter.SysRoleConverter;
import net.aopacloud.superbi.model.dto.SysRoleDTO;
import net.aopacloud.superbi.model.query.SysRoleQuery;
import net.aopacloud.superbi.model.vo.SysRoleVO;
import net.aopacloud.superbi.service.SysRoleService;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * system role
 *
 * @Author shinnie
 * @Description
 * @Date 10:22 2023/10/11
 */
@RestController
@CrossOrigin
@RequestMapping("sysRole")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService sysRoleService;

    private final SysRoleConverter sysRoleConverter;

    @GetMapping
    public RestApiResponse<List<SysRoleVO>> listAll(SysRoleQuery sysRoleQuery) {
        List<SysRoleDTO> list = sysRoleService.listAll(sysRoleQuery);
        return RestApiResponse.success(sysRoleConverter.toVOList(list));
    }

    @PostMapping
    @SuperAdminPermission
    public RestApiResponse<SysRoleVO> add(@RequestBody SysRoleVO sysRoleVO) {
        SysRoleDTO sysRoleDTO = sysRoleService.save(sysRoleConverter.toDTO(sysRoleVO));
        return RestApiResponse.success(sysRoleConverter.toVO(sysRoleDTO));
    }

    @PutMapping("/{id}")
    @SuperAdminPermission
    public RestApiResponse<SysRoleVO> update(@RequestBody SysRoleVO sysRoleVO, @PathVariable Long id) {
        sysRoleVO.setId(id);
        sysRoleVO.setUpdateTime(new Date());
        SysRoleDTO sysRoleDTO = sysRoleService.update(sysRoleConverter.toDTO(sysRoleVO));
        return RestApiResponse.success(sysRoleConverter.toVO(sysRoleDTO));
    }

    @DeleteMapping("/{id}")
    @SuperAdminPermission
    public RestApiResponse<SysRoleVO> delete(@PathVariable Long id) {
        SysRoleDTO sysRoleDTO = sysRoleService.delete(id);
        return RestApiResponse.success(sysRoleConverter.toVO(sysRoleDTO));
    }

    @GetMapping("/{id}")
    public RestApiResponse<SysRoleVO> findOne(@PathVariable Long id) {
        SysRoleDTO sysRoleDTO = sysRoleService.findOne(id);
        return RestApiResponse.success(sysRoleConverter.toVO(sysRoleDTO));
    }
}
