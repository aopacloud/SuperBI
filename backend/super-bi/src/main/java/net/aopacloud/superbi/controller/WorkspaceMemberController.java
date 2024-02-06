package net.aopacloud.superbi.controller;

import com.github.pagehelper.PageInfo;
import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import net.aopacloud.superbi.auth.ApiPermission;
import net.aopacloud.superbi.common.core.utils.PageUtils;
import net.aopacloud.superbi.common.core.web.domain.RestApiResponse;
import net.aopacloud.superbi.common.core.web.page.PageVO;
import net.aopacloud.superbi.model.converter.WorkspaceMemberConverter;
import net.aopacloud.superbi.model.dto.SysUserDTO;
import net.aopacloud.superbi.model.dto.WorkspaceMemberDTO;
import net.aopacloud.superbi.model.query.WorkspaceMemberQuery;
import net.aopacloud.superbi.model.vo.WorkspaceMemberVO;
import net.aopacloud.superbi.service.SysUserService;
import net.aopacloud.superbi.service.WorkspaceMemberService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Workspace Member
 *
 * @author: hudong
 * @date: 2023/8/9
 * @description:
 */
@RestController
@RequestMapping("workspace/member")
@RequiredArgsConstructor
public class WorkspaceMemberController {

    private final WorkspaceMemberService workspaceMemberService;

    private final SysUserService sysUserService;

    private final WorkspaceMemberConverter converter;

    /**
     * get workspace member list
     * no pagination
     *
     * @param query
     * @return
     */
    @GetMapping
    public RestApiResponse<PageVO<WorkspaceMemberVO>> search(WorkspaceMemberQuery query) {

        if (!Strings.isNullOrEmpty(query.getKeyword())) {
            List<SysUserDTO> userDTOS = sysUserService.filter(query.getKeyword());
            if (userDTOS != null && !userDTOS.isEmpty()) {
                query.setSearchUsers(userDTOS.stream().map(SysUserDTO::getUsername).collect(Collectors.toList()));
            } else {
                return RestApiResponse.success(new PageVO<>());
            }
        }
        PageUtils.startPage();
        List<WorkspaceMemberDTO> memberDTOS = workspaceMemberService.search(query);
        PageInfo pageInfo = new PageInfo(memberDTOS);
        PageVO page = new PageVO(converter.toVOList(memberDTOS), pageInfo.getTotal());

        return RestApiResponse.success(page);
    }

    /**
     * add member to workspace
     *
     * @param memberVOS
     * @return
     */
    @PostMapping
    @ApiPermission("WORKSPACE:VIEW:MANAGE:USER")
    public RestApiResponse<List<WorkspaceMemberVO>> add(@RequestBody List<WorkspaceMemberVO> memberVOS) {

        List<WorkspaceMemberDTO> memberDTOS = workspaceMemberService.save(converter.toDTOList(memberVOS));
        return RestApiResponse.success(converter.toVOList(memberDTOS));
    }


    /**
     * update member
     *
     * @param memberVO
     * @param id       member id
     * @return
     */
    @PutMapping("/{id}")
    @ApiPermission("WORKSPACE:VIEW:MANAGE:USER")
    public RestApiResponse<WorkspaceMemberVO> update(@RequestBody WorkspaceMemberVO memberVO, @PathVariable Long id) {
        memberVO.setId(id);
        WorkspaceMemberDTO memberDTO = workspaceMemberService.update(converter.toDTO(memberVO));
        return RestApiResponse.success(converter.toVO(memberDTO));
    }

    /**
     * delete member from workspace
     *
     * @param id member id
     * @return
     */
    @DeleteMapping("/{id}")
    @ApiPermission("WORKSPACE:VIEW:MANAGE:USER")
    public RestApiResponse<WorkspaceMemberVO> delete(@PathVariable Long id) {
        WorkspaceMemberDTO memberDTO = workspaceMemberService.delete(id);
        return RestApiResponse.success(converter.toVO(memberDTO));
    }

}
