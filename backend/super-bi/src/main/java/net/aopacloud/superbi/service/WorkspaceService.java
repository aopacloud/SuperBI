package net.aopacloud.superbi.service;

import net.aopacloud.superbi.model.dto.WorkspaceDTO;

import java.util.List;

public interface WorkspaceService {

    /**
     * get workspace by specified id
     *
     * @param id
     * @return
     */
    WorkspaceDTO findOne(Long id);

    /**
     * get all workspace list
     *
     * @return
     */
    List<WorkspaceDTO> listAll();


    /**
     * get workspace list that belong to me
     *
     * @return
     */
    List<WorkspaceDTO> listBelongMe();

    /**
     * create workspace
     *
     * @param workspaceDTO
     * @return
     */
    WorkspaceDTO save(WorkspaceDTO workspaceDTO);

    /**
     * update workspace
     *
     * @param workspaceDTO
     * @return
     */
    WorkspaceDTO update(WorkspaceDTO workspaceDTO);

    /**
     * delete workspace
     *
     * @param id
     * @return
     */
    WorkspaceDTO delete(Long id);

    /**
     * update workspace sort
     *
     * @param workspaceDTOS
     * @return
     */
    List<WorkspaceDTO> updateSort(List<WorkspaceDTO> workspaceDTOS);

    /**
     * get workspace list that can be managed
     *
     * @return
     */
    List<WorkspaceDTO> listCanManage();
}
