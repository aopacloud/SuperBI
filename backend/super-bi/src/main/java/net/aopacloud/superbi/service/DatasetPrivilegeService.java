package net.aopacloud.superbi.service;

import net.aopacloud.superbi.enums.PermissionEnum;
import net.aopacloud.superbi.model.dto.*;
import net.aopacloud.superbi.queryEngine.model.QueryParam;

import java.util.List;

public interface DatasetPrivilegeService {

    /**
     * get dataset max permission
     * @param dataset
     * @return READ means only read permission,
     *         WRITE means can modify dataset,
     *         NONE means no permission,
     *         EXPIRED means all permission is expired
     */
    PermissionEnum findDatasetMixedPermission(DatasetDTO dataset);

    /**
     * get dataset max permission
     * @param datasetId
     * @param username
     * @return READ means only read permission,
     *         WRITE means can modify dataset,
     *         NONE means no permission,
     *         EXPIRED means all permission is expired
     */
    PermissionEnum findDatasetMixedPermission(Long datasetId, String username);

    /**
     * fill dataset field privilege
     * @param datasetDTO
     * @param username
     * @return dataset filed list with privilege
     */
    List<DatasetFieldDTO> fillDatasetFieldAuthorize(DatasetDTO datasetDTO, String username);

    /**
     * fill dataset field privilege
     * @param dataset
     * @param username
     * @param isSuperAdmin
     * @param hasWorkspaceAllPrivilege
     * @return  dataset privilege detail
     */
    DatasetPrivilege checkDatasetPrivilege(DatasetDTO dataset, String username, boolean isSuperAdmin, boolean hasWorkspaceAllPrivilege);

    /**
     * check query privilege
     * if this current query can run return QueryPrivilege with pass true, otherwise return QueryPrivilege with pass false
     * @param queryParam
     * @param dataset
     * @param username
     * @return
     */
    QueryPrivilege checkQueryPrivilege(QueryParam queryParam, DatasetDTO dataset, String username);

    /**
     * check query privilege
     * if this current query can run return QueryPrivilege with pass true, otherwise return QueryPrivilege with pass false
     * @param queryParam
     * @param datasetPrivilege
     * @return
     */
    QueryPrivilege checkQueryPrivilege(DatasetPrivilege datasetPrivilege, QueryParam queryParam);
}
