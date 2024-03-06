package net.aopacloud.superbi.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.aopacloud.superbi.listener.event.AuthRoleUserUpdateEvent;
import net.aopacloud.superbi.mapper.AuthRoleUserMapper;
import net.aopacloud.superbi.model.converter.AuthRoleUserConverter;
import net.aopacloud.superbi.model.dto.AuthRoleUserDTO;
import net.aopacloud.superbi.model.entity.AuthRoleUser;
import net.aopacloud.superbi.service.AuthRoleUserService;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author shinnie
 * @Description
 * @Date 11:51 2023/8/17
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthRoleUserServiceImpl implements AuthRoleUserService {

    private final AuthRoleUserMapper authRoleUserMapper;

    private final AuthRoleUserConverter converter;

    private final ApplicationContext applicationContext;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<AuthRoleUserDTO> saveOrUpdate(Long roleId, List<String> users) {
        List<AuthRoleUser> authRoleUsers = users.stream().map(userName -> {
            AuthRoleUser authRoleUser = new AuthRoleUser();
            authRoleUser.setRoleId(roleId);
            authRoleUser.setUsername(userName);
            return authRoleUser;
        }).collect(Collectors.toList());
        authRoleUserMapper.deleteAllRoleUser(roleId);
        if (!authRoleUsers.isEmpty()) {
            authRoleUserMapper.batchInsert(authRoleUsers);
        }
        applicationContext.publishEvent(new AuthRoleUserUpdateEvent(roleId, authRoleUsers));
        return converter.entityToDTOList(authRoleUsers);
    }

    @Override
    public void delete(Long roleId, List<String> users) {
        authRoleUserMapper.deleteRoleUser(roleId, users);
    }

    @Override
    public List<String> getUserByRole(Long roleId) {
        return authRoleUserMapper.selectUserByRole(roleId);
    }

    @Override
    public List<AuthRoleUserDTO> getUserByRoleDetail(Long roleId) {
        return authRoleUserMapper.selectUserDetailByRole(roleId);
    }

}
