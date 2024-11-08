package net.aopacloud.superbi.service;

import net.aopacloud.superbi.common.core.web.page.PageVO;
import net.aopacloud.superbi.model.domain.TableDescriptor;
import net.aopacloud.superbi.model.dto.DatasetDTO;
import net.aopacloud.superbi.model.dto.DatasetFieldDTO;
import net.aopacloud.superbi.model.query.DatasetQuery;
import net.aopacloud.superbi.model.query.RecycleQuery;
import net.aopacloud.superbi.model.vo.DatasetVO;
import net.aopacloud.superbi.model.vo.FieldPreviewVO;
import net.aopacloud.superbi.model.vo.RecycleVO;

import java.util.List;

/**
 * @Author shinnie
 * @Description
 * @Date 14:13 2023/8/16
 */
public interface DatasetService {

    DatasetDTO findOne(Long id);

    DatasetDTO findOneWithoutFields(Long id);

    DatasetDTO findOne(Long id, Integer version);

    DatasetDTO findLastEditVersion(Long id);

    PageVO<DatasetVO> search(DatasetQuery datasetQuery);

    PageVO<RecycleVO> searchByRecycle(RecycleQuery recycleQuery);

    DatasetDTO save(DatasetDTO datasetDTO);

    DatasetDTO delete(Long id);

    void recycleDelete(Long id);

    void restore(Long id);

    DatasetDTO update(DatasetDTO dataset, Long id);

    void offline(Long id);

    void online(Long id);

    DatasetDTO publish(Long id, Integer version);

    void setDataset(Long id, DatasetDTO datasetDTO);

    void downloadDataset(Long id);

    List<Long> selectIdsByWorkspaceAndCreator(Long workspace, String creator);

    Integer countByTable(Long workspaceId, String datasourceName, String tableName);

    List<DatasetDTO> listByDatasourceAndTable(Long datasourceId, String tableName);

    Integer countByUsername(String username);

    void handOver(String fromUsername, String toUsername);

    void handOverById(Long id, String fromUsername, String toUsername);

    List<DatasetDTO> findDatasetCanAuthorize(Long workspaceId, String username);

    List<DatasetDTO> findByDashboard(Long dashboardId);

    List<DatasetDTO> findOnlineDataset(Long workspaceId);

    String previewSql(DatasetDTO dataset);

    FieldPreviewVO previewField(Long datasetId, List<TableDescriptor> tables, List<DatasetFieldDTO> fieldDTOS);
}
