package net.aopacloud.superbi.mapper;

import net.aopacloud.superbi.model.entity.SysRoleResource;
import org.apache.ibatis.annotations.Param;

public interface SysRoleResourceMapper {

    void saveOrUpdate(SysRoleResource sysRoleResource);

    SysRoleResource selectByRole(@Param("roleId") Long roleId);
}