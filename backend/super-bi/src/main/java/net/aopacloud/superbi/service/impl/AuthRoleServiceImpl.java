package net.aopacloud.superbi.service.impl;

import net.aopacloud.superbi.common.core.context.LoginContextHolder;
import net.aopacloud.superbi.common.core.exception.ServiceException;
import net.aopacloud.superbi.i18n.LocaleMessages;
import net.aopacloud.superbi.i18n.MessageConsist;
import net.aopacloud.superbi.mapper.AuthRoleMapper;
import net.aopacloud.superbi.mapper.AuthRoleUserMapper;
import net.aopacloud.superbi.model.converter.AuthRoleConverter;
import net.aopacloud.superbi.model.dto.AuthRoleDTO;
import net.aopacloud.superbi.model.entity.AuthRole;
import net.aopacloud.superbi.service.AuthRoleService;
import net.aopacloud.superbi.service.SysUserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author shinnie
 * @Description
 * @Date 11:41 2023/8/17
 */
@Service
public class AuthRoleServiceImpl implements AuthRoleService {

    private final AuthRoleMapper authRoleMapper;

    private final AuthRoleUserMapper authRoleUserMapper;

    private final AuthRoleConverter converter;

    private final SysUserService sysUserService;

    public AuthRoleServiceImpl(AuthRoleMapper roleMapper, AuthRoleUserMapper roleUserMapper, AuthRoleConverter converter, @Lazy SysUserService sysUserService) {
        this.authRoleMapper = roleMapper;
        this.authRoleUserMapper = roleUserMapper;
        this.converter = converter;
        this.sysUserService = sysUserService;
    }

    @Override
    public List<AuthRoleDTO> search(Long workspaceId, String keyword) {
        List<AuthRoleDTO> roles = authRoleMapper.selectAllByWorkspace(workspaceId, keyword);
        roles.stream().forEach(role -> {
            role.setUserNum(authRoleUserMapper.countByRoleId(role.getId()));
            role.setCreatorAliasName(sysUserService.getUserAliasName(role.getCreator()));
        });
        return roles;
    }

    @Override
    public AuthRoleDTO save(AuthRoleDTO authRoleDTO) {
        AuthRole exist = authRoleMapper.selectByNameByWorkspace(authRoleDTO.getName(), authRoleDTO.getWorkspaceId());
        if (exist != null) {
            throw new ServiceException(LocaleMessages.getMessage(MessageConsist.DUPLICATE_NAME_ERROR));
        }
        AuthRole authRole = converter.toEntity(authRoleDTO);
        authRole.setCreator(LoginContextHolder.getUsername());
        authRoleMapper.insert(authRole);
        return authRoleDTO;
    }

    @Override
    public AuthRoleDTO update(AuthRoleDTO authRoleDTO) {
        AuthRole exist = authRoleMapper.selectByNameByWorkspace(authRoleDTO.getName(), authRoleDTO.getWorkspaceId());
        if (exist != null && !exist.getId().equals(authRoleDTO.getId())) {
            throw new ServiceException(LocaleMessages.getMessage(MessageConsist.DUPLICATE_NAME_ERROR));
        }
        authRoleMapper.update(converter.toEntity(authRoleDTO));
        return authRoleDTO;
    }

    @Override
    public AuthRoleDTO delete(Long id) {
        AuthRole authRole = authRoleMapper.selectOneById(id);
        authRoleMapper.deleteById(id);
        authRoleUserMapper.deleteAllRoleUser(id);
        return converter.entityToDTO(authRole);
    }

    @Override
    public AuthRoleDTO findOne(Long id) {
        AuthRole authRole = authRoleMapper.selectOneById(id);
        return converter.entityToDTO(authRole);
    }

    @Override
    public Integer countByWorkspace(Long workspaceId) {
        return authRoleMapper.countByWorkspace(workspaceId);
    }

    @Override
    public List<AuthRoleDTO> search(String keyword) {
        return authRoleMapper.search(keyword);
    }

    @Override
    public List<AuthRoleDTO> listAll() {
        return authRoleMapper.selectAllByWorkspace(null, null);
    }
}
