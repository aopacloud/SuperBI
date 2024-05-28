package net.aopacloud.superbi.mapper;

import net.aopacloud.superbi.enums.PermissionEnum;
import net.aopacloud.superbi.model.dto.DatasetAuthorizeDTO;
import net.aopacloud.superbi.model.entity.DatasetAuthorize;
import net.aopacloud.superbi.model.query.BaseQuery;
import net.aopacloud.superbi.model.query.DatasetAuthorizeBatchQuery;
import net.aopacloud.superbi.model.query.DatasetAuthorizeQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public interface DatasetAuthorizeMapper {
    List<DatasetAuthorizeDTO> searchUserAuthorize(BaseQuery query);

    List<DatasetAuthorizeDTO> searchRoleAuthorize(DatasetAuthorizeQuery query);

    void softDelete(Long id);

    void insert(DatasetAuthorize datasetAuthorize);

    void update(DatasetAuthorize datasetAuthorize);

    DatasetAuthorize selectOneById(Long id);

    List<DatasetAuthorize> selectActiveByDatasetAndUsername(@Param("datasetId") Long datasetId, @Param("username") String username);

    List<DatasetAuthorize> selectActiveByWorkspaceAndUsername(@Param("workspaceId") Long workspaceId, @Param("username") String username);

    List<DatasetAuthorize> selectByDatasetAndUsername(@Param("datasetId") Long datasetId, @Param("username") String username);

    List<DatasetAuthorizeDTO> selectMaybeExpire();

    void updateAuthorizeExpire(Long id);

    List<DatasetAuthorizeDTO> findActivedAuthorize(@Param("username") String username, @Param("workspaceId") Long workspaceId);

    List<DatasetAuthorize> selectUserAuthorizeByDatasetAndUsername(@Param("datasetId") Long datasetId, @Param("username") String username);

    List<DatasetAuthorizeDTO> selectAuthorizeByRole(@Param("roleId") Long roleId);

    List<DatasetAuthorizeDTO> selectWriteAuthorize(@Param("username") String username, @Param("workspaceId") Long workspaceId);

    List<DatasetAuthorizeDTO> selectAuthorizeByUsername(@Param("username") String username);

    List<DatasetAuthorizeDTO> selectAuthorizeByDataset(@Param("datasetId") Long datasetId);

    List<DatasetAuthorizeDTO> search(DatasetAuthorizeBatchQuery query);

    List<DatasetAuthorizeDTO> selectAuthorizeByDatasetsAndUsername(@Param("datasetIds") Set<Long> datasetIds, @Param("username") String username);
    List<DatasetAuthorizeDTO> selectAuthorizeByDatasetsAndRole(@Param("datasetIds") Set<Long> datasetIds,  @Param("roleId") Long roleId);

    List<DatasetAuthorizeDTO> selectByPermission(@Param("workspaceId") Long workspaceId, @Param(("username")) String username, @Param("permission") PermissionEnum write);

    void deleteByUsername(String username);
}
