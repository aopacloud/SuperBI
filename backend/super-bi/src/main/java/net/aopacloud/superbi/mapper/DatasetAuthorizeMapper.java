package net.aopacloud.superbi.mapper;

import net.aopacloud.superbi.model.dto.DatasetAuthorizeDTO;
import net.aopacloud.superbi.model.entity.DatasetAuthorize;
import net.aopacloud.superbi.model.query.BaseQuery;
import net.aopacloud.superbi.model.query.DatasetAuthorizeQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DatasetAuthorizeMapper {
    List<DatasetAuthorizeDTO> searchUserAuthorize(BaseQuery query);

    List<DatasetAuthorizeDTO> searchRoleAuthorize(DatasetAuthorizeQuery query);

    void softDelete(Long id);

    void insert(DatasetAuthorize datasetAuthorize);

    void update(DatasetAuthorize datasetAuthorize);

    DatasetAuthorize selectOneById(Long id);

    List<DatasetAuthorize> selectActiveByDatasetAndUsername(@Param("datasetId") Long datasetId, @Param("username") String username);

    List<DatasetAuthorize> selectByDatasetAndUsername(@Param("datasetId") Long datasetId, @Param("username") String username);

    List<DatasetAuthorizeDTO> selectMaybeExpire();

    void updateAuthorizeExpire(Long id);

    List<DatasetAuthorizeDTO> findActivedAuthorize(@Param("username") String username,@Param("workspaceId") Long workspaceId);

    List<DatasetAuthorize> selectUserAuthorizeByDatasetAndUsername(@Param("datasetId") Long datasetId, @Param("username") String username);
}
