package net.aopacloud.superbi.controller;

import lombok.RequiredArgsConstructor;
import net.aopacloud.superbi.common.core.context.LoginContextHolder;
import net.aopacloud.superbi.common.core.web.domain.RestApiResponse;
import net.aopacloud.superbi.model.converter.SysUserConverter;
import net.aopacloud.superbi.model.dto.SysUserDTO;
import net.aopacloud.superbi.model.uo.ResetPasswordUO;
import net.aopacloud.superbi.model.uo.SysUserUO;
import net.aopacloud.superbi.model.vo.SysUserVO;
import net.aopacloud.superbi.service.SysUserService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/12/10 14:03
 */
@RestController
@RequestMapping("sysProfile")
@RequiredArgsConstructor
public class SysProfileController {
    private final SysUserService sysUserService;
    private final SysUserConverter sysUserConverter;
    private static final long MAX_AVATAR_SIZE = 5 * 1024 * 1024; // 5MB

    @PostMapping("/avatar")
    public RestApiResponse<SysUserVO> avatar(@RequestParam("avatarFile") MultipartFile avatarFile) {
        if (!avatarFile.isEmpty()) {
            if (avatarFile.getSize() > MAX_AVATAR_SIZE) {
                return RestApiResponse.error("上传图片大小不能超过5MB");
            }
            SysUserDTO sysUserDTO = sysUserService.saveAvatar(avatarFile);
            return RestApiResponse.success(sysUserConverter.toVO(sysUserDTO));
        }
        return RestApiResponse.error("上传图片异常，请联系管理员");
    }

    @PutMapping("/password")
    public RestApiResponse<String> updatePassword(@RequestBody ResetPasswordUO resetPasswordUO) {
        sysUserService.updatePassword(resetPasswordUO);
        return RestApiResponse.success("修改成功");
    }

    @PutMapping("/email")
    public RestApiResponse<String> updateEmail(@RequestBody SysUserUO sysUserUO) {
        SysUserUO newSysUserUO = new SysUserUO();
        newSysUserUO.setEmail(sysUserUO.getEmail());
        newSysUserUO.setUsername(LoginContextHolder.getUsername());
        newSysUserUO.setUuid(sysUserUO.getUuid());
        newSysUserUO.setCode(sysUserUO.getCode());
        sysUserService.updateProfile(newSysUserUO);
        return RestApiResponse.success("修改成功");
    }

    @PutMapping("/mobile")
    public RestApiResponse<String> updateMobile(@RequestBody SysUserUO sysUserUO) {
        SysUserUO newSysUserUO = new SysUserUO();
        newSysUserUO.setMobile(sysUserUO.getMobile());
        newSysUserUO.setUsername(LoginContextHolder.getUsername());
        newSysUserUO.setUuid(sysUserUO.getUuid());
        newSysUserUO.setCode(sysUserUO.getCode());
        sysUserService.updateProfile(newSysUserUO);
        return RestApiResponse.success("修改成功");
    }
}
