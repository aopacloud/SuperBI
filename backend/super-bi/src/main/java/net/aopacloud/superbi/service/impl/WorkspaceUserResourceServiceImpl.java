package net.aopacloud.superbi.service.impl;

import lombok.RequiredArgsConstructor;
import net.aopacloud.superbi.common.core.context.LoginContextHolder;
import net.aopacloud.superbi.constant.BiConsist;
import net.aopacloud.superbi.model.dto.SysMenuDTO;
import net.aopacloud.superbi.model.dto.SysRoleDTO;
import net.aopacloud.superbi.model.dto.SysRoleResourceDTO;
import net.aopacloud.superbi.model.dto.WorkspaceUserResourceDTO;
import net.aopacloud.superbi.service.*;
import org.assertj.core.util.Sets;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkspaceUserResourceServiceImpl implements WorkspaceUserResourceService {

    private final SysRoleService sysRoleService;

    private final SysRoleMenuService sysRoleMenuService;

    private final SysRoleResourceService sysRoleResourceService;

    private final SysUserService sysUserService;

    private final SysMenuService sysMenuService;

    @Override
    public WorkspaceUserResourceDTO get(Long workspaceId) {

        String currentUser = LoginContextHolder.getUsername();

        WorkspaceUserResourceDTO workspaceUserResourceDTO = new WorkspaceUserResourceDTO();
        workspaceUserResourceDTO.setWorkspaceId(workspaceId);
        workspaceUserResourceDTO.setUsername(currentUser);

        if (sysUserService.isSuperAdmin(currentUser)) {

            workspaceUserResourceDTO.setMenus(sysMenuService.listAll());
            workspaceUserResourceDTO.setResourceCodes(Arrays.stream(BiConsist.SYSTEM_ADMIN_ROLE_RESOURCE_CODE.split(",")).collect(Collectors.toSet()));

            return workspaceUserResourceDTO;
        }

        SysRoleDTO sysRoleDTO = sysRoleService.findByUserAndWorkspace(currentUser, workspaceId);

        if (Objects.isNull(sysRoleDTO)) {
            return workspaceUserResourceDTO;
        }

        List<SysMenuDTO> menus = sysRoleMenuService.findByRole(sysRoleDTO);
        workspaceUserResourceDTO.setMenus(menus);

        SysRoleResourceDTO sysRoleResourceDTO = sysRoleResourceService.findByRole(sysRoleDTO.getId());
        if (!Objects.isNull(sysRoleResourceDTO)) {
            workspaceUserResourceDTO.setResourceCodes(sysRoleResourceDTO.getResourceCode());
        }
        return workspaceUserResourceDTO;
    }

    @Override
    public Set<String> getResourceCodes(Long workspaceId) {

        String currentUser = LoginContextHolder.getUsername();

        SysRoleDTO sysRoleDTO = sysRoleService.findByUserAndWorkspace(currentUser, workspaceId);

        if (Objects.isNull(sysRoleDTO)) {
            return Sets.newHashSet();
        }

        SysRoleResourceDTO sysRoleResourceDTO = sysRoleResourceService.findByRole(sysRoleDTO.getId());

        if (Objects.isNull(sysRoleResourceDTO) || Objects.isNull(sysRoleResourceDTO.getResourceCode())) {
            return Sets.newHashSet();
        }

        return sysRoleResourceDTO.getResourceCode();
    }
}
