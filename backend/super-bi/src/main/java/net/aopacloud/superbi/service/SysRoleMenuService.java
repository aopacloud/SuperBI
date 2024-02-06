package net.aopacloud.superbi.service;

import net.aopacloud.superbi.model.dto.SysMenuDTO;
import net.aopacloud.superbi.model.dto.SysRoleDTO;
import net.aopacloud.superbi.model.dto.SysRoleMenuDTO;

import java.util.List;

public interface SysRoleMenuService {
    void saveOrUpdate(List<SysRoleMenuDTO> sysRoleMenuDTOS);

    List<SysMenuDTO> findByRoleId(Long roleId);

    List<SysMenuDTO> findByRole(SysRoleDTO sysRole);
}
