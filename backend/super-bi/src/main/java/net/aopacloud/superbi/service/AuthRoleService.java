package net.aopacloud.superbi.service;

import net.aopacloud.superbi.model.dto.AuthRoleDTO;
import net.aopacloud.superbi.model.entity.AuthRole;

import java.util.List;

/**
 * @Author shinnie
 * @Description
 * @Date 11:40 2023/8/17
 */
public interface AuthRoleService {

    List<AuthRoleDTO> search(Long workspaceId, String keyword);

    AuthRoleDTO save(AuthRoleDTO authRoleDTO);

    AuthRoleDTO update(AuthRoleDTO authRoleDTO);

    AuthRoleDTO delete(Long id);

    AuthRoleDTO findOne(Long id);

    Integer countByWorkspace(Long workspaceId);

    List<AuthRoleDTO> search(String keyword);

    List<AuthRoleDTO> searchByUsernames(List<String> usernames);
    List<AuthRoleDTO> listAll();
}
