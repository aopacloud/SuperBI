package net.aopacloud.superbi.service.impl;

import net.aopacloud.superbi.common.core.context.LoginContextHolder;
import net.aopacloud.superbi.common.core.exception.ObjectNotFoundException;
import net.aopacloud.superbi.common.core.exception.ServiceException;
import net.aopacloud.superbi.constant.BiConsist;
import net.aopacloud.superbi.enums.MemberLevelEnum;
import net.aopacloud.superbi.i18n.LocaleMessages;
import net.aopacloud.superbi.i18n.MessageConsist;
import net.aopacloud.superbi.mapper.WorkspaceConfigMapper;
import net.aopacloud.superbi.mapper.WorkspaceMapper;
import net.aopacloud.superbi.mapper.WorkspaceMemberMapper;
import net.aopacloud.superbi.model.converter.WorkspaceConfigConverter;
import net.aopacloud.superbi.model.converter.WorkspaceConverter;
import net.aopacloud.superbi.model.dto.SysUserDTO;
import net.aopacloud.superbi.model.dto.WorkspaceConfigDTO;
import net.aopacloud.superbi.model.dto.WorkspaceDTO;
import net.aopacloud.superbi.model.entity.Workspace;
import net.aopacloud.superbi.model.entity.WorkspaceConfig;
import net.aopacloud.superbi.model.entity.WorkspaceMember;
import net.aopacloud.superbi.service.*;
import org.apache.commons.compress.utils.Lists;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author: hudong
 * @date: 2023/8/4
 * @description:
 */
@Service
public class WorkspaceServiceImpl implements WorkspaceService {

    private final WorkspaceMapper workspaceMapper;

    private final WorkspaceConfigMapper workspaceConfigMapper;

    private final WorkspaceMemberMapper workspaceMemberMapper;

    private final WorkspaceConverter converter;

    private final WorkspaceConfigConverter configConverter;

    private final AuthRoleService authRoleService;

    private final SysUserService sysUserService;

    private final SysAdminService sysAdminService;

    private final SysRoleResourceService sysRoleResourceService;

    public WorkspaceServiceImpl(WorkspaceMapper workspaceMapper, WorkspaceConfigMapper workspaceConfigMapper, WorkspaceMemberMapper workspaceMemberMapper, WorkspaceConverter converter, WorkspaceConfigConverter configConverter, AuthRoleService roleService, @Lazy SysUserService sysUserService, SysAdminService sysAdminService, SysRoleResourceService sysRoleResourceService) {
        this.workspaceMapper = workspaceMapper;
        this.workspaceConfigMapper = workspaceConfigMapper;
        this.workspaceMemberMapper = workspaceMemberMapper;
        this.converter = converter;
        this.configConverter = configConverter;
        this.authRoleService = roleService;
        this.sysUserService = sysUserService;
        this.sysAdminService = sysAdminService;
        this.sysRoleResourceService = sysRoleResourceService;
    }

    @Override
    public WorkspaceDTO findOne(Long id) {

        Workspace workspace = Optional.ofNullable(workspaceMapper.selectOneById(id)).orElseThrow(() -> new ObjectNotFoundException());

        WorkspaceConfig config = workspaceConfigMapper.selectOneByWorkspaceId(workspace.getId());

        Integer memberCount = workspaceMemberMapper.countByWorkspace(workspace.getId());

        Integer roleCount = authRoleService.countByWorkspace(workspace.getId());

        List<WorkspaceMember> admins = workspaceMemberMapper.selectByWorkspaceAndLevel(workspace.getId(), MemberLevelEnum.ADMIN);
        List<SysUserDTO> adminUsers = admins.stream().map(member -> sysUserService.get(member.getUsername())).collect(Collectors.toList());

        WorkspaceDTO workspaceDTO = converter.toDTO(workspace, config, adminUsers, memberCount, roleCount);

        return workspaceDTO;
    }

    @Override
    public List<WorkspaceDTO> listAll() {

        List<Workspace> workspaces = workspaceMapper.selectAll();

        List<WorkspaceDTO> workspaceDTOS = converter.entityToDTOList(workspaces);

        return workspaceDTOS;
    }

    @Override
    public List<WorkspaceDTO> listCanManage() {


        List<Workspace> workspaces = workspaceMapper.selectAll();

        if (sysAdminService.isSuperAdmin(LoginContextHolder.getUsername())) {
            return converter.entityToDTOList(workspaces);
        }

        workspaces = workspaces.stream().filter(workspace -> {
            List<WorkspaceMember> workspaceMembers = workspaceMemberMapper.selectByWorkspaceAndUsername(workspace.getId(), LoginContextHolder.getUsername());
            Set<String> resourceCodes = workspaceMembers.stream()
                    .filter(member -> !Objects.isNull(member.getSysRoleId()))
                    .map(member -> sysRoleResourceService.findByRole(member.getSysRoleId()))
                    .filter(sysRoleResource -> !Objects.isNull(sysRoleResource))
                    .flatMap(sysRoleResource -> sysRoleResource.getResourceCode().stream())
                    .collect(Collectors.toSet());

            return resourceCodes.contains(BiConsist.WORKSPACE_MANAGE_USER) || resourceCodes.contains(BiConsist.WORKSPACE_MANAGE_ROLE);

        }).collect(Collectors.toList());

        List<WorkspaceDTO> workspaceDTOS = converter.entityToDTOList(workspaces);

        return workspaceDTOS;
    }

    @Override
    public List<WorkspaceDTO> listBelongMe() {
       return listBelongMe(LoginContextHolder.getUsername());
    }

    @Override
    public List<WorkspaceDTO> listBelongMe(String username) {

        List<Workspace> belongMeWorkspace = Lists.newArrayList();
        if (sysAdminService.isSuperAdmin(username)) {
            belongMeWorkspace = workspaceMapper.selectAll();
        } else {
            belongMeWorkspace = workspaceMapper.selectAllBelongMe(username);
        }

        return belongMeWorkspace.stream().map(workspace -> {
            WorkspaceDTO workspaceDTO = converter.entityToDTO(workspace);
            WorkspaceConfig workspaceConfig = workspaceConfigMapper.selectOneByWorkspaceId(workspace.getId());
            workspaceDTO.setConfig(configConverter.entityToDTO(workspaceConfig));
            return workspaceDTO;
        }).collect(Collectors.toList());
    }

    @Override
    public WorkspaceDTO save(WorkspaceDTO workspaceDTO) {

        Workspace exists = workspaceMapper.selectOneByName(workspaceDTO.getName());
        if (exists != null) {
            throw new ServiceException(LocaleMessages.getMessage(MessageConsist.DUPLICATE_NAME_ERROR));
        }

        Workspace workspace = converter.toEntity(workspaceDTO);
        workspaceMapper.insert(workspace);

        WorkspaceConfigDTO config = WorkspaceConfigDTO.create();
        config.setWorkspaceId(workspace.getId());
        workspaceConfigMapper.insert(configConverter.toEntity(config));

        return workspaceDTO;
    }

    @Override
    public WorkspaceDTO update(WorkspaceDTO workspaceDTO) {

        Workspace exists = workspaceMapper.selectOneByName(workspaceDTO.getName());
        if (exists != null && !exists.getId().equals(workspaceDTO.getId())) {
            throw new ServiceException(LocaleMessages.getMessage(MessageConsist.DUPLICATE_NAME_ERROR));
        }

        Workspace workspace = converter.toEntity(workspaceDTO);
        workspaceMapper.update(workspace);

        WorkspaceConfigDTO config = workspaceDTO.getConfig();
        workspaceConfigMapper.update(configConverter.toEntity(config));

        return workspaceDTO;
    }

    @Override
    public WorkspaceDTO delete(Long id) {

        WorkspaceDTO workspaceDTO = findOne(id);
        workspaceMapper.deleteById(id);
        if (workspaceDTO.getConfig() != null) {
            workspaceConfigMapper.deleteById(workspaceDTO.getConfig().getId());
        }
        return workspaceDTO;
    }

    @Override
    public List<WorkspaceDTO> updateSort(List<WorkspaceDTO> workspaceDTOS) {
        workspaceDTOS.forEach(workspaceDTO -> {
            Workspace workspace = converter.toEntity(workspaceDTO);
            workspaceMapper.updateSort(workspace);
        });

        return listAll();
    }


}
