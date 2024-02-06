package net.aopacloud.superbi.service;

import net.aopacloud.superbi.model.dto.DatasetAuthorizeDTO;
import net.aopacloud.superbi.model.query.DatasetAuthorizeQuery;
import net.aopacloud.superbi.model.query.DatasetAuthorizeSaveVO;

import java.util.List;

/**
 * Dataset Authorize
 */
public interface DatasetAuthorizeService {
    /**
     * get user authorize list
     *
     * @param query
     * @return
     */
    List<DatasetAuthorizeDTO> searchUserAuthorize(DatasetAuthorizeQuery query);

    /**
     * get role authorize list
     *
     * @param query
     * @return
     */
    List<DatasetAuthorizeDTO> searchRoleAuthorize(DatasetAuthorizeQuery query);

    /**
     * create authorize
     *
     * @param datasetAuthorizeSaveVO
     * @return
     */
    List<DatasetAuthorizeDTO> save(DatasetAuthorizeSaveVO datasetAuthorizeSaveVO);

    DatasetAuthorizeDTO save(DatasetAuthorizeDTO datasetAuthorizeDTO);

    /**
     * update authorize
     *
     * @param datasetAuthorizeDTO
     * @return
     */
    DatasetAuthorizeDTO update(DatasetAuthorizeDTO datasetAuthorizeDTO);

    /**
     * delete authorize
     *
     * @param id
     * @return
     */
    DatasetAuthorizeDTO delete(Long id);

    /**
     * renew authorize
     *
     * @param datasetAuthorizeDTO
     * @return
     */
    DatasetAuthorizeDTO renewed(DatasetAuthorizeDTO datasetAuthorizeDTO);

    /**
     * get specified id authorize
     *
     * @param id
     * @return
     */
    DatasetAuthorizeDTO findOne(Long id);

    /**
     * get maybe expire authorize
     *
     * @return
     */
    List<DatasetAuthorizeDTO> findMaybeExpire();

    /**
     * update authorize to expire
     *
     * @param id
     */
    void updateAuthorizeExpire(Long id);

    List<DatasetAuthorizeDTO> findActivedAuthorize(String username, Long workspaceId);

    DatasetAuthorizeDTO updatePermission(DatasetAuthorizeDTO datasetAuthorizeDTO);

    DatasetAuthorizeDTO findLastOne(Long datasetId, String username);

    List<DatasetAuthorizeDTO> findAuthorizeByRole(Long roleId);

    List<DatasetAuthorizeDTO> findAuthorizeByUsername(String username);

    List<DatasetAuthorizeDTO> findAuthorizeByDataset(Long datasetId);
}
