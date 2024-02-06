package net.aopacloud.superbi.mapper;

import net.aopacloud.superbi.model.dto.SysMenuDTO;

import java.util.List;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/11/28 15:09
 */
public interface SysMenuMapper {
    List<SysMenuDTO> selectAll();
}
