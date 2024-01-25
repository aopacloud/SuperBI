package net.aopacloud.superbi.service.impl;

import net.aopacloud.superbi.i18n.LocaleMessages;
import net.aopacloud.superbi.model.converter.SysUserConverter;
import net.aopacloud.superbi.model.dto.SysUserDTO;
import net.aopacloud.superbi.model.entity.SysUser;
import net.aopacloud.superbi.exception.ServiceException;
import net.aopacloud.superbi.model.entity.LoginUser;
import net.aopacloud.superbi.service.SysPasswordService;
import net.aopacloud.superbi.service.SysPermissionService;
import net.aopacloud.superbi.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/11/29 20:43
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {
    // TODO 不推荐使用其他service中的mapper对象
    private final SysUserService sysUserService;
    private final SysPasswordService passwordService;
    private final SysPermissionService permissionService;
    private final SysUserConverter sysUserConverter;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUserDTO userDTO = sysUserService.get(username);
        if (userDTO == null) {
            log.info("登录用户：{} 不存在.", username);
            throw new ServiceException(LocaleMessages.getMessage("user.not.exist"));
        }
        // TODO 登陆用户已被删除、停用
//        else if (UserStatus.DELETED.getCode().equals(userDTO.getStatus())) {
//
//        }
        SysUser user = sysUserConverter.toEntity(userDTO);
        passwordService.validate(user);

        return createUserByUsername(user);
    }

    public UserDetails createUserByUsername(SysUser user) {
        return new LoginUser(user.getId(), user, permissionService.getMenuPermission(user));
    }
}
