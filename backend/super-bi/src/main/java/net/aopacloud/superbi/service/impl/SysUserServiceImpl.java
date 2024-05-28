package net.aopacloud.superbi.service.impl;

import com.github.pagehelper.PageInfo;
import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.aopacloud.superbi.cache.AuthorizeThreadLocalCache;
import net.aopacloud.superbi.common.core.context.LoginContextHolder;
import net.aopacloud.superbi.common.core.utils.PageUtils;
import net.aopacloud.superbi.common.core.utils.PasswordUtils;
import net.aopacloud.superbi.common.core.utils.StringUtils;
import net.aopacloud.superbi.common.core.utils.bean.BeanUtils;
import net.aopacloud.superbi.common.core.web.page.PageVO;
import net.aopacloud.superbi.common.redis.service.RedisService;
import net.aopacloud.superbi.constant.CacheConstants;
import net.aopacloud.superbi.exception.user.CaptchaException;
import net.aopacloud.superbi.exception.user.CaptchaExpireException;
import net.aopacloud.superbi.exception.user.UserPasswordNotMatchException;
import net.aopacloud.superbi.mapper.SysUserMapper;
import net.aopacloud.superbi.model.converter.SysUserConverter;
import net.aopacloud.superbi.model.dto.SysUserDTO;
import net.aopacloud.superbi.model.dto.TokenDTO;
import net.aopacloud.superbi.model.dto.UserInfoDTO;
import net.aopacloud.superbi.model.dto.WorkspaceDTO;
import net.aopacloud.superbi.model.entity.SysUser;
import net.aopacloud.superbi.model.query.SysUserQuery;
import net.aopacloud.superbi.model.uo.LoginBodyUO;
import net.aopacloud.superbi.model.uo.ResetPasswordUO;
import net.aopacloud.superbi.model.uo.SysUserUO;
import net.aopacloud.superbi.model.vo.SysUserVO;
import net.aopacloud.superbi.service.SysAdminService;
import net.aopacloud.superbi.service.SysUserService;
import net.aopacloud.superbi.service.WorkspaceService;
import net.aopacloud.superbi.utils.SecurityUtils;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * @author: hudong
 * @date: 2023/8/3
 * @description:
 */
@Service
@AllArgsConstructor
@Slf4j
public class SysUserServiceImpl implements SysUserService {

    private final SysUserMapper sysUserMapper;

    private final SysUserConverter sysUserConverter;

    private final WorkspaceService workspaceService;

    private final SysAdminService sysAdminService;

    private final RedisService redisService;

    @SneakyThrows
    @Override
    public SysUserDTO get(String username) {
        return sysUserMapper.selectSysUserByUsername(username);
    }

    @Override
    public TokenDTO login(LoginBodyUO loginBodyUO) {
        return null;
    }

    @Override
    public PageVO<SysUserVO> search(SysUserQuery sysUserQuery) {
        PageUtils.startPage();
        List<SysUserDTO> sysUserDTOS = sysUserMapper.search(sysUserQuery);

        PageInfo pageInfo = new PageInfo(sysUserDTOS);
        return new PageVO(sysUserConverter.toVOList(sysUserDTOS), pageInfo.getTotal());
    }

    @Override
    public SysUserDTO updateProfile(SysUserUO sysUserUO) {
        validateCaptcha(sysUserUO.getUuid(), sysUserUO.getCode());
        sysUserMapper.updateByUsername(sysUserUO);
        return sysUserMapper.selectSysUserByUsername(sysUserUO.getUsername());
    }

    @Override
    public SysUserDTO updateLoginInfo(SysUserDTO sysUserDTO) {
        sysUserMapper.updateLastLoginInfo(sysUserDTO);
        return sysUserMapper.selectSysUserByUsername(sysUserDTO.getUsername());
    }

    @Override
    public SysUserDTO update(SysUserUO sysUserUO) {
        sysUserMapper.updateByUsername(sysUserUO);
        return sysUserMapper.selectSysUserByUsername(sysUserUO.getUsername());
    }

    public void validateCaptcha(String uuid, String code) {
        String captchaRedisKey = CacheConstants.CAPTCHA_CODE_KEY + StringUtils.nvl(uuid, "");
        String captchaCode = redisService.getCacheObject(captchaRedisKey);
        redisService.deleteObject(captchaRedisKey);
        if (null == captchaCode) {
            throw new CaptchaExpireException();
        }
        if (!captchaCode.equalsIgnoreCase(code)) {
            throw new CaptchaException();
        }
    }

    @Override
    public List<SysUserDTO> insertBatch(List<SysUserUO> sysUserUOS) {
        String currentUsername = LoginContextHolder.getUsername();
        List<SysUser> sysUsers = new ArrayList<>();
        List<SysUserDTO> sysUserDTOS = new ArrayList<>();
        sysUserUOS.forEach(sysUserUO -> {
            SysUser sysUser = new SysUser();
            sysUser.setUsername(sysUserUO.getUsername());
            sysUser.setMobile(sysUserUO.getMobile());
            sysUser.setEmail(sysUserUO.getEmail());
            sysUser.setAliasName(sysUserUO.getAliasName());
            sysUser.setCreator(currentUsername);
            sysUser.setOperator(currentUsername);
            String originPassword = PasswordUtils.generateRandomPassword();
            String encryptPassword = SecurityUtils.encryptPassword(originPassword);
            sysUser.setPassword(encryptPassword);
            sysUsers.add(sysUser);
            SysUserDTO sysUserDTO = new SysUserDTO();
            sysUserDTO.setUsername(sysUserUO.getUsername());
            sysUserDTO.setPassword(originPassword);
            sysUserDTOS.add(sysUserDTO);
        });
        try {
            sysUserMapper.insertBatch(sysUsers);
        } catch (DuplicateKeyException e) {
            log.error(e.getMessage(), e);
            throw new IllegalArgumentException("用户名已存在");
        }
        return sysUserDTOS;
    }

    @Override
    public SysUserDTO resetPassword(String username) {
        String newPassword = PasswordUtils.generateRandomPassword();
        int updatedCount = sysUserMapper.updatePasswordByUsername(username, SecurityUtils.encryptPassword(newPassword));
        if (updatedCount < 1) {
            throw new IllegalArgumentException("该用户不存在");
        } else {
            SysUserDTO sysUserDTO = sysUserMapper.selectSysUserByUsername(username);
            sysUserDTO.setPassword(newPassword);
            return sysUserDTO;
        }
    }

    @Override
    public SysUserDTO deleteByUsername(String username) {
        SysUserDTO sysUserDTO = sysUserMapper.selectSysUserByUsername(username);
        sysUserMapper.deleteByUsername(username);
        return sysUserDTO;
    }


    private final static Integer IMAGE_HEIGHT = 256;
    private final static Integer IMAGE_WIDTH = 256;

    public byte[] resizeImage(MultipartFile avatarFile) {
        try {
            BufferedImage image = ImageIO.read(avatarFile.getInputStream());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Thumbnails.of(image).size(IMAGE_WIDTH, IMAGE_HEIGHT)
                    .outputFormat("jpg")
                    .toOutputStream(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SysUserDTO saveAvatar(MultipartFile avatarFile) {
        String currentUser = LoginContextHolder.getUsername();
        byte[] resizedAvatar = resizeImage(avatarFile);
        String avatar = Base64.getEncoder().encodeToString(resizedAvatar);
        SysUserUO sysUserUO = new SysUserUO();
        sysUserUO.setAvatar(avatar);
        sysUserUO.setUsername(currentUser);
        sysUserMapper.updateByUsername(sysUserUO);
        return sysUserMapper.selectSysUserByUsername(currentUser);
    }

    @Override
    public void updatePassword(ResetPasswordUO resetPasswordUO) {
        validateCaptcha(resetPasswordUO.getUuid(), resetPasswordUO.getCode());
        String currentUser = LoginContextHolder.getUsername();
        SysUserDTO sysUserDTO = sysUserMapper.selectSysUserByUsername(currentUser);
        if (!SecurityUtils.matchesPassword(resetPasswordUO.getOldPassword(), sysUserDTO.getPassword())) {
            throw new UserPasswordNotMatchException();
        }
        sysUserMapper.updatePasswordByUsername(currentUser, SecurityUtils.encryptPassword(resetPasswordUO.getNewPassword()));
    }

    @Override
    public UserInfoDTO whoAmI() {
        String currentUser = LoginContextHolder.getUsername();
        SysUserDTO userDTO = sysUserMapper.selectSysUserByUsername(currentUser);

        List<WorkspaceDTO> workspaceDTOS = workspaceService.listBelongMe();

        UserInfoDTO userInfoDTO = new UserInfoDTO();
        BeanUtils.copyBeanProp(userInfoDTO, userDTO);

        userInfoDTO.setWorkspaces(workspaceDTOS)
                .setIsSuperAdmin(isSuperAdmin(currentUser));

        return userInfoDTO;
    }

    @Override
    public boolean isSuperAdmin(String username) {
        Boolean superAdmin = AuthorizeThreadLocalCache.getSuperAdmin();
        if(Objects.isNull(superAdmin)) {
            superAdmin = sysAdminService.isSuperAdmin(username);
            AuthorizeThreadLocalCache.setSuperAdmin(superAdmin);
        }
        return superAdmin;
    }

    @Override
    public boolean isActive(String username) {
        SysUserDTO sysUserDTO = get(username);

        if(Objects.isNull(sysUserDTO)) {
            return Boolean.FALSE;
        }

        return Boolean.TRUE;
    }

    @Override
    public List<SysUserDTO> filter(String keyword) {
        SysUserQuery sysUserQuery = new SysUserQuery();
        sysUserQuery.setKeyword(keyword);
        return sysUserMapper.search(sysUserQuery);
    }

    @Override
    public String getUserAliasName(String username) {

        if (Strings.isNullOrEmpty(username)) {
            return username;
        }

        Optional<SysUserDTO> sysUserDTO = Optional.ofNullable(get(username));
        if (sysUserDTO.isPresent()) {
            String aliasName = sysUserDTO.get().getAliasName();
            if (!Strings.isNullOrEmpty(aliasName)) {
                return String.format("%s(%s)", aliasName, username);
            }
        }
        return username;
    }

}
