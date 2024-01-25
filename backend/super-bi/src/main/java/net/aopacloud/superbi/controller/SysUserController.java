package net.aopacloud.superbi.controller;

import net.aopacloud.superbi.auth.SuperAdminPermission;
import net.aopacloud.superbi.common.core.utils.poi.ExcelUtil;
import net.aopacloud.superbi.common.core.web.domain.RestApiResponse;
import net.aopacloud.superbi.common.core.web.page.PageVO;
import net.aopacloud.superbi.model.converter.SysUserConverter;
import net.aopacloud.superbi.model.converter.SysUserPasswordConverter;
import net.aopacloud.superbi.model.converter.UserInfoConverter;
import net.aopacloud.superbi.model.dto.SysUserDTO;
import net.aopacloud.superbi.model.dto.UserInfoDTO;
import net.aopacloud.superbi.model.query.SysUserQuery;
import net.aopacloud.superbi.model.uo.HandOverUO;
import net.aopacloud.superbi.model.uo.SysUserUO;
import net.aopacloud.superbi.model.vo.SysUserPasswordVO;
import net.aopacloud.superbi.model.vo.SysUserVO;
import net.aopacloud.superbi.model.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import net.aopacloud.superbi.service.HandOverService;
import org.springframework.web.bind.annotation.*;
import net.aopacloud.superbi.service.SysUserService;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/12/6 11:02
 */
@RestController
@RequestMapping("sysUser")
@RequiredArgsConstructor
public class SysUserController {
    private final SysUserService sysUserService;
    private final HandOverService handOverService;
    private final UserInfoConverter userInfoConverter;
    private final SysUserConverter sysUserConverter;
    private final SysUserPasswordConverter sysUserPasswordConverter;

    @GetMapping("/list")
    public RestApiResponse<PageVO<SysUserVO>> search(SysUserQuery sysUserQuery) {
        PageVO page = sysUserService.search(sysUserQuery);
        return RestApiResponse.success(page);
    }

    @GetMapping("/whoami")
    public RestApiResponse<UserInfoVO> whoAmI() {
        UserInfoDTO userInfoDTO = sysUserService.whoAmI();
        return RestApiResponse.success(userInfoConverter.toVO(userInfoDTO));
    }

    /**
     * 检查用户下是否存在资源
     * @return
     */
    @GetMapping("/{username}/existResources")
    public RestApiResponse<Boolean> existResources(@PathVariable("username") String username) {
        return RestApiResponse.success(handOverService.checkUserHasResource(username));
    }

    /**
     * 移交资源给其他用户
     * @return
     */
    @PutMapping("/{username}/transferResources")
    @SuperAdminPermission
    public RestApiResponse<Boolean> transferResources(@PathVariable("username") String username,@RequestBody HandOverUO handOverUO) {
        handOverService.moveUserResource(username, handOverUO.getToUsername(), handOverUO.getPosition());
        return RestApiResponse.success(Boolean.FALSE);
    }

    @DeleteMapping("{username}/transferResources")
    @SuperAdminPermission
    public RestApiResponse<SysUserVO> transferResourcesAndDeleteUser(@PathVariable("username") String username, @RequestBody HandOverUO handOverUO) {
        handOverService.moveUserResource(username, handOverUO.getToUsername(), handOverUO.getPosition());
        SysUserDTO syUserDTO = sysUserService.deleteByUsername(username);
        return RestApiResponse.success(sysUserConverter.toVO(syUserDTO));
    }

    /**
     * 删除用户
     * @return
     */
    @DeleteMapping("/{username}")
    @SuperAdminPermission
    public RestApiResponse<SysUserVO> deleteUser(@PathVariable("username") String username) {
        // TODO 需要加上资源检测，rick，2023-12-08
        SysUserDTO syUserDTO = sysUserService.deleteByUsername(username);
        return RestApiResponse.success(sysUserConverter.toVO(syUserDTO));
    }

    @PutMapping("/{username}/password")
    @SuperAdminPermission
    public void resetPassword(HttpServletResponse response, @PathVariable("username") String username) {
        // TODO 需要记录修改人是谁，rick，2023-12-08
        SysUserDTO sysUserDTO = sysUserService.resetPassword(username);
        List<SysUserPasswordVO> sysUserPasswordVOS = new ArrayList<>();
        sysUserPasswordVOS.add(sysUserPasswordConverter.toVO(sysUserDTO));
        ExcelUtil<SysUserPasswordVO> util = new ExcelUtil<SysUserPasswordVO>(SysUserPasswordVO.class);
        util.exportExcel(response, sysUserPasswordVOS, "用户密码");
    }

    /**
     * 批量添加用户
     * @param sysUserUOList
     * @return
     */
    @PostMapping("/users")
    @SuperAdminPermission
    public void addUsers(HttpServletResponse response, @RequestBody List<SysUserUO> sysUserUOList) {
        List<SysUserDTO> sysUserDTOS = sysUserService.insertBatch(sysUserUOList);
        List<SysUserPasswordVO> sysUserPasswordVOS = sysUserPasswordConverter.toVOList(sysUserDTOS);
        ExcelUtil<SysUserPasswordVO> util = new ExcelUtil<SysUserPasswordVO>(SysUserPasswordVO.class);
        util.exportExcel(response, sysUserPasswordVOS, "用户密码");
    }

    /**
     * 修改用户信息
     * @param username
     * @param sysUserUO
     * @return
     */
    @PutMapping("/{username}")
    public RestApiResponse<SysUserVO> updateUser(@PathVariable("username") String username, @RequestBody SysUserUO sysUserUO) {
        sysUserUO.setUsername(username);
        return RestApiResponse.success(sysUserConverter.toVO(sysUserService.update(sysUserUO)));
    }

    @PutMapping("/avatar")
    public RestApiResponse<Boolean> addAvatar() {
        return RestApiResponse.success(Boolean.TRUE);
    }

    @GetMapping("/avatar")
    public RestApiResponse<Boolean> getAvatar() {
        return RestApiResponse.success(Boolean.TRUE);
    }
}
