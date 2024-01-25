package net.aopacloud.superbi.service;

import net.aopacloud.superbi.model.entity.SysUser;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/11/29 20:45
 */
public interface SysPasswordService {
    void validate(SysUser sysUser);
}
