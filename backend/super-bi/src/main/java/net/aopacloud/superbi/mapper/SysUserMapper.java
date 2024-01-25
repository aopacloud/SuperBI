package net.aopacloud.superbi.mapper;

import net.aopacloud.superbi.model.dto.SysUserDTO;
import net.aopacloud.superbi.model.entity.SysUser;
import net.aopacloud.superbi.model.query.SysUserQuery;
import net.aopacloud.superbi.model.uo.SysUserUO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/11/28 15:11
 */
public interface SysUserMapper {
    SysUserDTO selectSysUserByUsername(String username);

    int updateByUsername(SysUserUO sysUserUO);

    int insertBatch(List<SysUser> sysUserUOS);

    int deleteByUsername(String username);

    int updatePasswordByUsername(@Param("username") String username, @Param("password") String password);

    List<SysUserDTO> search(SysUserQuery sysUserQuery);
}
