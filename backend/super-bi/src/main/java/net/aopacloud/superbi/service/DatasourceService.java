package net.aopacloud.superbi.service;

import net.aopacloud.superbi.model.dto.ConnectResultDTO;
import net.aopacloud.superbi.model.dto.DatasetDTO;
import net.aopacloud.superbi.model.dto.DatasourceDTO;
import net.aopacloud.superbi.model.dto.TableInfoDTO;

import java.util.List;

/**
 * @Author shinnie
 * @Description
 * @Date 10:21 2023/8/28
 */
public interface DatasourceService {

    List<DatasourceDTO> listAll(Long workspaceId, String keyword);

    DatasourceDTO save(DatasourceDTO datasourceDTO);

    DatasourceDTO update(DatasourceDTO datasourceDTO);

    DatasourceDTO delete(Long id);

    ConnectResultDTO connectTest(DatasourceDTO datasourceDTO);

    List<TableInfoDTO> listTableInfo(Long datasourceId);

    TableInfoDTO getTableInfo(Long datasourceId, String tableName);

    DatasourceDTO getOne(Long id);

    DatasourceDTO getOneByWorkspaceAndName(Long workspaceId, String name);

    List<DatasetDTO> getDatasetByDatasourceAndTable(Long id, String tableName);
}
