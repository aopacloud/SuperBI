package net.aopacloud.superbi.service;

import net.aopacloud.superbi.model.dto.WorkspaceMemberDTO;
import net.aopacloud.superbi.model.query.BaseQuery;

import java.util.List;

/**
 * @author: hudong
 * @date: 2023/8/9
 * @description:
 */
public interface WorkspaceMemberService {

    /**
     * get all member by workspace and query
     * @param baseQuery
     * @return
     */
    List<WorkspaceMemberDTO> search(BaseQuery baseQuery);

    /**
     * add member
     * @param memberDTOS
     * @return
     */
    List<WorkspaceMemberDTO> save(List<WorkspaceMemberDTO> memberDTOS);

    /**
     * update member
     * @param workspaceMemberDTO
     * @return
     */
    WorkspaceMemberDTO update(WorkspaceMemberDTO workspaceMemberDTO);

    /**
     * delete member
     * @param id
     * @return
     */
    WorkspaceMemberDTO delete(Long id);
}
