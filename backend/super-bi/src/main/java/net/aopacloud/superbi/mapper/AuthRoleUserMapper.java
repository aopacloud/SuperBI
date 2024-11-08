package net.aopacloud.superbi.mapper;

import net.aopacloud.superbi.model.dto.AuthRoleUserDTO;
import net.aopacloud.superbi.model.entity.AuthRoleUser;

import java.util.List;

public interface AuthRoleUserMapper {

    int deleteAllRoleUser(Long roleId);

    int deleteRoleUser(Long roleId, List<String> users);

    int batchInsert(List<AuthRoleUser> list);

    int countByRoleId(Long roleId);

    List<String> selectUserByRole(Long roleId);

    List<AuthRoleUserDTO> selectUserDetailByRole(Long roleId);

    void deleteByUsername(String username);
}