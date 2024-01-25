package net.aopacloud.superbi.controller;

import net.aopacloud.superbi.common.core.utils.PageUtils;
import net.aopacloud.superbi.common.core.web.domain.RestApiResponse;
import net.aopacloud.superbi.common.core.web.page.PageVO;
import net.aopacloud.superbi.auth.ApiPermission;
import net.aopacloud.superbi.model.converter.AuthRoleConverter;
import net.aopacloud.superbi.model.dto.AuthRoleDTO;
import net.aopacloud.superbi.model.query.BaseQuery;
import net.aopacloud.superbi.model.vo.AuthRoleVO;
import net.aopacloud.superbi.service.AuthRoleService;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Authority Role
 * @Author shinnie
 * @Description
 * @Date 11:40 2023/8/17
 */
@RestController
@RequestMapping("authRole")
@RequiredArgsConstructor
public class AuthRoleController {

    private final AuthRoleService authRoleService;

    private final AuthRoleConverter authRoleConverter;

    /**
     * get role list
     * @param query
     * @return
     */
    @GetMapping
    public RestApiResponse<PageVO<AuthRoleVO>> listAll(BaseQuery query) {
        PageUtils.startPage();
        List<AuthRoleDTO> authRoleDTOS = authRoleService.listAll(query.getWorkspaceId(), query.getKeyword());

        PageInfo pageInfo = new PageInfo(authRoleDTOS);
        PageVO page = new PageVO(authRoleConverter.toVOList(authRoleDTOS), pageInfo.getTotal());
        return RestApiResponse.success(page);
    }

    /**
     * add role
     * @param authRoleVO
     * @return
     */
    @PostMapping
    @ApiPermission("WORKSPACE:VIEW:MANAGE:ROLE")
    public RestApiResponse<AuthRoleVO> add(@RequestBody AuthRoleVO authRoleVO) {
        AuthRoleDTO authRoleDTO = authRoleService.save(authRoleConverter.toDTO(authRoleVO));
        return RestApiResponse.success(authRoleConverter.toVO(authRoleDTO));
    }

    /**
     * update role
     *
     * @param authRoleVO
     * @param id
     * @return
     */
    @PutMapping("/{id}")
    @ApiPermission("WORKSPACE:VIEW:MANAGE:ROLE")
    public RestApiResponse<AuthRoleVO> update(@RequestBody AuthRoleVO authRoleVO, @PathVariable Long id) {
        authRoleVO.setId(id);
        AuthRoleDTO authRoleDTO = authRoleService.update(authRoleConverter.toDTO(authRoleVO));
        return RestApiResponse.success(authRoleConverter.toVO(authRoleDTO));
    }


    /**
     * delete role
     * @param id role id
     * @return
     */
    @DeleteMapping("/{id}")
    @ApiPermission("WORKSPACE:VIEW:MANAGE:ROLE")
    public RestApiResponse<AuthRoleVO> delete(@PathVariable Long id) {
        AuthRoleDTO authRoleDTO = authRoleService.delete(id);
        return RestApiResponse.success(authRoleConverter.toVO(authRoleDTO));
    }

}
