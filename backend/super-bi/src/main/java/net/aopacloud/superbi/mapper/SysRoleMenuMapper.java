package net.aopacloud.superbi.mapper;

import net.aopacloud.superbi.model.dto.SysMenuDTO;
import net.aopacloud.superbi.model.entity.SysRoleMenu;

import java.util.List;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/11/28 15:12
 */
public interface SysRoleMenuMapper {
    void deleteByRoleId(Long roleId);

    void insert(SysRoleMenu sysRoleMenu);

    List<SysMenuDTO> selectByRoleId(Long roleId);
}
