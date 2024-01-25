package net.aopacloud.superbi.service.impl;

import net.aopacloud.superbi.model.entity.SysUser;
import net.aopacloud.superbi.service.SysMenuService;
import net.aopacloud.superbi.service.SysRoleService;
import net.aopacloud.superbi.service.SysPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/11/29 20:57
 */
@Service
@RequiredArgsConstructor
public class SysPermissionServiceImpl implements SysPermissionService {
    private final SysRoleService sysRoleService;
    private final SysMenuService sysMenuService;
    @Override
    public Set<String> getRolePermission(SysUser user) {
        return sysRoleService.selectRolePermissionByUserId(user.getId());
    }

    @Override
    public Set<String> getMenuPermission(SysUser user) {
        return null;
    }
}
