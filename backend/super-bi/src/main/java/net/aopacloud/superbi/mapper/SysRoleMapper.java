package net.aopacloud.superbi.mapper;

import net.aopacloud.superbi.model.dto.SysRoleDTO;
import net.aopacloud.superbi.model.entity.SysRole;
import net.aopacloud.superbi.model.query.SysRoleQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysRoleMapper {

    List<SysRole> queryRole(SysRoleQuery sysRoleQuery);

    int isRoleExist(@Param("name") String name, @Param("id") Long id);

    int deleteById(Long id);

    int insert(SysRole row);

    SysRole selectById(Long id);

    int updateByIdSelective(SysRole row);

    int updateById(SysRole row);

    SysRoleDTO findByUserAndWorkspace(@Param("username") String username, @Param("workspaceId") Long workspaceId);
}