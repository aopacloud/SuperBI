package net.aopacloud.superbi.service.impl;

import lombok.RequiredArgsConstructor;
import net.aopacloud.superbi.common.core.context.LoginContextHolder;
import net.aopacloud.superbi.constant.BiConsist;
import net.aopacloud.superbi.enums.PermissionEnum;
import net.aopacloud.superbi.mapper.DashboardMapper;
import net.aopacloud.superbi.mapper.DashboardShareMapper;
import net.aopacloud.superbi.mapper.DatasetAuthorizeMapper;
import net.aopacloud.superbi.mapper.DatasetMapper;
import net.aopacloud.superbi.model.dto.*;
import net.aopacloud.superbi.service.*;
import org.apache.commons.compress.utils.Lists;
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

    private final DatasetAuthorizeMapper datasetAuthorizeMapper;

    private final DatasetMapper datasetMapper;

    private final DashboardMapper dashboardMapper;

    private final DashboardShareMapper dashboardShareMapper;

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


    @Override
    public List<Long> getCanMangeDatasetIds(Long workspaceId, String username) {

        if(sysUserService.isSuperAdmin(username) ) {
            return datasetMapper.selectIdsByWorkspaceAndCreator(workspaceId, null);
        }

        Set<String> resourceCodes = getResourceCodes(workspaceId);
        if (resourceCodes.contains(BiConsist.DATASET_MANAGE_ALL_WORKSPACE_CODE)) {
            return datasetMapper.selectIdsByWorkspaceAndCreator(workspaceId, null);
        }

        Set<Long> datasetIds = Sets.newHashSet();
        if (resourceCodes.contains(BiConsist.DATASET_MANAGE_HAS_PRIVILEGE_CODE)) {
            List<DatasetAuthorizeDTO> datasetAuthorizeDTOS = datasetAuthorizeMapper.selectByPermission(workspaceId, username, PermissionEnum.WRITE );
            Set<Long> ids = datasetAuthorizeDTOS.stream().map(DatasetAuthorizeDTO::getDatasetId).collect(Collectors.toSet());
            datasetIds.addAll(ids);
        }

        datasetIds.addAll(datasetMapper.selectIdsByWorkspaceAndCreator(workspaceId, username));

        return datasetIds.stream().collect(Collectors.toList());
    }


    @Override
    public List<Long> getCanMangeDashboardIds(Long workspaceId, String username) {

        if(sysUserService.isSuperAdmin(username) ) {
            return dashboardMapper.selectIdByWorkspaceAndCreator(workspaceId, null );
        }

        Set<String> resourceCodes = getResourceCodes(workspaceId);
        if (resourceCodes.contains(BiConsist.DATASET_MANAGE_ALL_WORKSPACE_CODE)) {
            return dashboardMapper.selectIdByWorkspaceAndCreator(workspaceId, null);
        }

        Set<Long> dashboardIds = Sets.newHashSet();
        if (resourceCodes.contains(BiConsist.DATASET_MANAGE_HAS_PRIVILEGE_CODE)) {
            List<DashboardShareDTO> datasetAuthorizeDTOS = dashboardShareMapper.selectByPermission(workspaceId, username, PermissionEnum.WRITE );
            Set<Long> ids = datasetAuthorizeDTOS.stream().map(DashboardShareDTO::getDashboardId).collect(Collectors.toSet());
            dashboardIds.addAll(ids);
        }

        dashboardIds.addAll(dashboardMapper.selectIdByWorkspaceAndCreator(workspaceId, username));

        return dashboardIds.stream().collect(Collectors.toList());
    }


}
