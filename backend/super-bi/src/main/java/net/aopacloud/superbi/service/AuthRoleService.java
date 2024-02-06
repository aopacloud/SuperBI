package net.aopacloud.superbi.service;

import net.aopacloud.superbi.model.dto.AuthRoleDTO;

import java.util.List;

/**
 * @Author shinnie
 * @Description
 * @Date 11:40 2023/8/17
 */
public interface AuthRoleService {

    List<AuthRoleDTO> listAll(Long workspaceId, String keyword);

    AuthRoleDTO save(AuthRoleDTO authRoleDTO);

    AuthRoleDTO update(AuthRoleDTO authRoleDTO);

    AuthRoleDTO delete(Long id);

    AuthRoleDTO findOne(Long id);

    Integer countByWorkspace(Long workspaceId);
}
