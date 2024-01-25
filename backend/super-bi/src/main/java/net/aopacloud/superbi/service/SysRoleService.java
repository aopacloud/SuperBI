package net.aopacloud.superbi.service;

import net.aopacloud.superbi.model.dto.SysRoleDTO;
import net.aopacloud.superbi.model.query.SysRoleQuery;

import java.util.List;
import java.util.Set;

/**
 * @Author shinnie
 * @Description
 * @Date 10:23 2023/10/11
 */
public interface SysRoleService {

    List<SysRoleDTO> listAll(SysRoleQuery sysRoleQuery);

    SysRoleDTO save(SysRoleDTO sysRoleDTO);

    SysRoleDTO update(SysRoleDTO sysRoleDTO);

    SysRoleDTO delete(Long id);

    SysRoleDTO findOne(Long id);

    SysRoleDTO findByUserAndWorkspace(String username, Long workspaceId);

    Set<String> selectRolePermissionByUserId(Long userId);
}
