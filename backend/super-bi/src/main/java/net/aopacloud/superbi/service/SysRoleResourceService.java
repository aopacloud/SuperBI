package net.aopacloud.superbi.service;

import net.aopacloud.superbi.model.dto.SysRoleResourceDTO;

/**
 * @Author shinnie
 * @Description
 * @Date 11:32 2023/10/11
 */
public interface SysRoleResourceService {

    SysRoleResourceDTO saveOrUpdate(SysRoleResourceDTO sysRoleResourceDTO);

    SysRoleResourceDTO findByRole(Long roleId);
}
