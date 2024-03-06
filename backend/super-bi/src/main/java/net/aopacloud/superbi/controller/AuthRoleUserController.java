package net.aopacloud.superbi.controller;

import lombok.RequiredArgsConstructor;
import net.aopacloud.superbi.auth.ApiPermission;
import net.aopacloud.superbi.common.core.web.domain.RestApiResponse;
import net.aopacloud.superbi.model.converter.AuthRoleUserConverter;
import net.aopacloud.superbi.model.dto.AuthRoleUserDTO;
import net.aopacloud.superbi.model.vo.AuthRoleUserVO;
import net.aopacloud.superbi.service.AuthRoleUserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author shinnie
 * @Description
 * @Date 11:42 2023/8/17
 */
@RestController
@RequestMapping("authRole/{roleId}/user")
@RequiredArgsConstructor
public class AuthRoleUserController {

    private final AuthRoleUserService authRoleUserService;

    private final AuthRoleUserConverter converter;

    /**
     * add user to role
     *
     * @param roleId role id
     * @param users  user id list
     * @return
     */
    @PostMapping
    @ApiPermission("WORKSPACE:VIEW:MANAGE:ROLE")
    public RestApiResponse saveOrUpdate(@RequestBody List<String> users, @PathVariable Long roleId) {
        List<AuthRoleUserDTO> authRoleUserDTOS = authRoleUserService.saveOrUpdate(roleId, users);
        return RestApiResponse.success(converter.toVOList(authRoleUserDTOS));
    }

    /**
     * delete user from role
     *
     * @param roleId role id
     * @param users  user id list
     * @return
     */
    @DeleteMapping()
    @ApiPermission("WORKSPACE:VIEW:MANAGE:ROLE")
    public RestApiResponse delete(@RequestBody List<String> users, @PathVariable Long roleId) {
        authRoleUserService.delete(roleId, users);
        return RestApiResponse.success();
    }

    @GetMapping
    public RestApiResponse<List<String>> getRoleUser(@PathVariable Long roleId) {
        return RestApiResponse.success(authRoleUserService.getUserByRole(roleId));
    }

    @GetMapping("/detail")
    public RestApiResponse<List<AuthRoleUserVO>> getRoleUserDetail(@PathVariable Long roleId) {
        return RestApiResponse.success(converter.toVOList(authRoleUserService.getUserByRoleDetail(roleId)));
    }

}
