package net.aopacloud.superbi.mapper;


import net.aopacloud.superbi.enums.StatusEnum;
import net.aopacloud.superbi.model.dto.DatasetDTO;
import net.aopacloud.superbi.model.entity.Dataset;
import net.aopacloud.superbi.model.query.ConditionQuery;
import net.aopacloud.superbi.model.query.DatasetQuery;
import net.aopacloud.superbi.model.query.RecycleQuery;
import net.aopacloud.superbi.model.vo.RecycleVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @author: hu.dong
 * @date: 2021/10/25
 **/
public interface DatasetMapper {

    Dataset selectById(@Param("id") Long id);

    List<Dataset> selectAll();

    Dataset selectByIdRecycle(@Param("id") Long id);

    List<DatasetDTO> search(DatasetQuery query);

    List<RecycleVO> searchByRecycle(RecycleQuery query);

    List<DatasetDTO> findMyFavorite(ConditionQuery query);

    List<DatasetDTO> findMyCreate(ConditionQuery query);

    List<Long> findMyCreateId(@Param("username") String username);

    List<DatasetDTO> findHasPermission(ConditionQuery query);

    List<DatasetDTO> findUnclassified(ConditionQuery query);

    List<DatasetDTO> findMyAll(ConditionQuery query);

    List<DatasetDTO> findList(ConditionQuery query);

    void save(Dataset dataset);

    void update(Dataset dataset);

    void deleteById(@Param("id") Long id);

    void recycleDelete(@Param("id") Long id);

    void restore(@Param("id") Long id);

    void updateStatus(@Param("id") Long id, @Param("status") StatusEnum status);

    void updateLastEditVersion(@Param("id") Long id, @Param("lastEditVersion") int lastEditVersion);

    void updateApplyEnable(@Param("id") Long id, @Param("enableApply") int enableApply);

    List<Long> selectIdsByWorkspaceAndCreator(@Param("workspaceId") Long workspaceId, @Param("creator") String creator);

    Integer countByTable(@Param("workspaceId") Long workspaceId, @Param("datasourceName") String datasourceName, @Param("tableName") String tableName);

    List<DatasetDTO> selectByDatasourceAndTable(@Param("datasourceId") Long datasourceId, @Param("tableName") String tableName);

    Integer countByUsername(@Param("username") String username);

    void updateCreator(@Param("fromUsername") String fromUsername, @Param("toUsername") String toUsername);

    void updateOperatorById(@Param("id") Long id, @Param("operator") String operator);

    void updateCreatorById(@Param("id") Long id, @Param("fromUsername") String fromUsername, @Param("toUsername") String toUsername);

    List<DatasetDTO> findListByWorkspace(@Param("workspaceId") Long workspaceId);

    List<DatasetDTO> selectByIdsOrCreator(@Param("datasetIds") Set<Long> datasetIds, @Param("username") String username);

    List<DatasetDTO> findByDashboard(@Param("dashboardId") Long dashboardId);

    List<DatasetDTO> selectOnlineDataset(@Param("workspaceId") Long workspaceId);
}
