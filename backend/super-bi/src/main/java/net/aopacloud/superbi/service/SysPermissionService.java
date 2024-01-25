package net.aopacloud.superbi.service;

import net.aopacloud.superbi.model.entity.SysUser;

import java.util.Set;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/11/29 20:56
 */
public interface SysPermissionService {
    Set<String> getRolePermission(SysUser user);

    Set<String> getMenuPermission(SysUser user);
}
