package net.aopacloud.superbi.mapper;


import net.aopacloud.superbi.enums.StatusEnum;
import net.aopacloud.superbi.model.dto.DatasetDTO;
import net.aopacloud.superbi.model.entity.Dataset;
import net.aopacloud.superbi.model.query.ConditionQuery;
import net.aopacloud.superbi.model.query.DatasetQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @author: hu.dong
 * @date: 2021/10/25
 **/
public interface DatasetMapper {

    Dataset selectById(@Param("id") Long id);

    List<DatasetDTO> search(DatasetQuery query);

    List<DatasetDTO> findMyFavorite(ConditionQuery query);

    List<DatasetDTO> findMyCreate(ConditionQuery query);

    List<DatasetDTO> findHasPermission(ConditionQuery query);

    List<DatasetDTO> findUnclassified(ConditionQuery query);

    List<DatasetDTO> findMyAll(ConditionQuery query);

    List<DatasetDTO> findList(ConditionQuery query);

    void save(Dataset dataset);

    void update(Dataset dataset);

    void deleteById(@Param("id") Long id);

    void updateStatus(@Param("id") Long id, @Param("status") StatusEnum status);

    void updateLastEditVersion(@Param("id") Long id, @Param("lastEditVersion") int lastEditVersion);

    void updateApplyEnable(@Param("id") Long id, @Param("enableApply") int enableApply);

    List<Long> selectIdsByWorkspaceAndCreator(@Param("workspaceId") Long workspaceId, @Param("creator") String creator);

    Integer countByTable(@Param("workspaceId") Long workspaceId, @Param("datasourceName") String datasourceName, @Param("tableName") String tableName);

    List<DatasetDTO> selectByDatasourceAndTable(@Param("datasourceId") Long datasourceId, @Param("tableName") String tableName);

    Integer countByUsername(@Param("username") String username);

    void updateCreator(@Param("fromUsername") String fromUsername, @Param("toUsername") String toUsername);

    List<DatasetDTO> findListByWorkspace(@Param("workspaceId") Long workspaceId);

    List<DatasetDTO> selectByIdsOrCreator(@Param("datasetIds") Set<Long> datasetIds, @Param("username") String username);

    List<DatasetDTO> findByDashboard(@Param("dashboardId") Long dashboardId);
}
