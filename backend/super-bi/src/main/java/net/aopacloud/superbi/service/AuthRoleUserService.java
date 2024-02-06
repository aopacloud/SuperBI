package net.aopacloud.superbi.service;

import net.aopacloud.superbi.model.dto.AuthRoleUserDTO;

import java.util.List;

/**
 * @Author shinnie
 * @Description
 * @Date 11:50 2023/8/17
 */
public interface AuthRoleUserService {

    List<AuthRoleUserDTO> saveOrUpdate(Long roleId, List<String> users);

    void delete(Long roleId, List<String> users);

    List<String> getUserByRole(Long roleId);

}
