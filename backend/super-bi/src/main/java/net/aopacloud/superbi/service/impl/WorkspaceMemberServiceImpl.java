package net.aopacloud.superbi.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.aopacloud.superbi.enums.MemberLevelEnum;
import net.aopacloud.superbi.mapper.WorkspaceMemberMapper;
import net.aopacloud.superbi.model.converter.WorkspaceMemberConverter;
import net.aopacloud.superbi.model.dto.WorkspaceMemberDTO;
import net.aopacloud.superbi.model.entity.WorkspaceMember;
import net.aopacloud.superbi.model.query.BaseQuery;
import net.aopacloud.superbi.service.SysUserService;
import net.aopacloud.superbi.service.WorkspaceMemberService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: hudong
 * @date: 2023/8/9
 * @description:
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WorkspaceMemberServiceImpl implements WorkspaceMemberService {

    private final WorkspaceMemberMapper workspaceMemberMapper;

    private final WorkspaceMemberConverter converter;

    private final SysUserService sysUserService;

    @Override
    public List<WorkspaceMemberDTO> search(BaseQuery baseQuery) {
        List<WorkspaceMemberDTO> members = workspaceMemberMapper.search(baseQuery);
        members.stream().forEach(member -> member.setAliasName(sysUserService.getUserAliasName(member.getUsername())));
        return members;
    }

    @Override
    public List<WorkspaceMemberDTO> save(List<WorkspaceMemberDTO> memberDTOS) {
        List<WorkspaceMember> workspaceMembers = memberDTOS.stream().map(converter::toEntity).collect(Collectors.toList());
        for (WorkspaceMember member : workspaceMembers) {
            member.setLevel(MemberLevelEnum.ANALYZER.name());
            member.setJoinTime(new Date());
            workspaceMemberMapper.insert(member);
        }
        return converter.entityToDTOList(workspaceMembers);
    }

    @Override
    public WorkspaceMemberDTO update(WorkspaceMemberDTO workspaceMemberDTO) {

        workspaceMemberMapper.update(converter.toEntity(workspaceMemberDTO));

        return workspaceMemberDTO;
    }

    @Override
    public WorkspaceMemberDTO delete(Long id) {

        WorkspaceMember workspaceMember = workspaceMemberMapper.selectOneById(id);
        workspaceMemberMapper.deleteById(id);
        log.info("delete workspace member {}, workspace id = {}", workspaceMember.getUsername(), workspaceMember.getWorkspaceId());
        return converter.entityToDTO(workspaceMember);
    }

    @Override
    public void deleteByUsername(String username) {
        log.warn("delete {} from all workspace", username);
        workspaceMemberMapper.deleteByUsername(username);
    }

}
