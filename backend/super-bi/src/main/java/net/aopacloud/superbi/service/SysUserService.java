package net.aopacloud.superbi.service;

import net.aopacloud.superbi.common.core.web.page.PageVO;
import net.aopacloud.superbi.model.dto.SysUserDTO;
import net.aopacloud.superbi.model.dto.TokenDTO;
import net.aopacloud.superbi.model.dto.UserInfoDTO;
import net.aopacloud.superbi.model.query.SysUserQuery;
import net.aopacloud.superbi.model.uo.LoginBodyUO;
import net.aopacloud.superbi.model.uo.ResetPasswordUO;
import net.aopacloud.superbi.model.uo.SysUserUO;
import net.aopacloud.superbi.model.vo.SysUserVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author: hudong
 * @date: 2023/8/3
 * @description:
 */
public interface SysUserService {

    /**
     * get user by username
     *
     * @param username
     * @return
     */
    SysUserDTO get(String username);

    TokenDTO login(LoginBodyUO loginBodyUO);

    PageVO<SysUserVO> search(SysUserQuery sysUserQuery);

    SysUserDTO updateProfile(SysUserUO sysUserUO);

    SysUserDTO update(SysUserUO sysUserUO);

    List<SysUserDTO> insertBatch(List<SysUserUO> sysUserUOS);

    SysUserDTO resetPassword(String username);

    SysUserDTO deleteByUsername(String username);

    SysUserDTO saveAvatar(MultipartFile avatarFile);

    void updatePassword(ResetPasswordUO resetPasswordUO);

    UserInfoDTO whoAmI();

    boolean isSuperAdmin(String username);

    List<SysUserDTO> filter(String keyword);

    /**
     * get all user
     *
     * @return
     */
//    List<SysUserDTO> getAll();

    /**
     * get user alias name
     *
     * @param username
     * @return
     */
    String getUserAliasName(String username);

//    default List<SysUserDTO> search(String keyword) {
//        if(Strings.isNullOrEmpty(keyword)){
//            return Lists.newArrayList();
//        }
//        return getAll().stream().filter(user -> user.getAliasName().contains(keyword) || user.getUsername().contains(keyword)).collect(Collectors.toList());
//    }
}
