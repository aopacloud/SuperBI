package net.aopacloud.superbi.mapper;

import net.aopacloud.superbi.enums.MemberLevelEnum;
import net.aopacloud.superbi.model.dto.WorkspaceMemberDTO;
import net.aopacloud.superbi.model.entity.WorkspaceMember;
import net.aopacloud.superbi.model.query.BaseQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WorkspaceMemberMapper {
    Integer countByWorkspace(Long workspaceId);

    List<WorkspaceMember> selectByWorkspaceAndLevel(@Param("workspaceId") Long workspaceId, @Param("level") MemberLevelEnum level);

    List<WorkspaceMemberDTO> search(BaseQuery baseQuery);

    void insert(WorkspaceMember member);

    void update(WorkspaceMember member);

    WorkspaceMember selectOneById(Long id);

    void deleteById(Long id);

    List<WorkspaceMember> selectByWorkspaceAndUsername(@Param("workspaceId") Long workspaceId, @Param("username") String username);
}
