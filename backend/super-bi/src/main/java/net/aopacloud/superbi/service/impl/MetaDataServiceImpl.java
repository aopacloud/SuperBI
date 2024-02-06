package net.aopacloud.superbi.service.impl;

import com.google.common.collect.Lists;
import net.aopacloud.superbi.common.core.utils.bean.BeanUtils;
import net.aopacloud.superbi.model.dto.*;
import net.aopacloud.superbi.queryEngine.metadata.MetaDataBackend;
import net.aopacloud.superbi.service.DatasourceService;
import net.aopacloud.superbi.service.MetaDataService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: hudong
 * @date: 2023/10/23
 * @description:
 */
@Service
public class MetaDataServiceImpl implements MetaDataService {

    private final DatasourceService datasourceService;

    private final MetaDataBackend metaDataBackend;


    public MetaDataServiceImpl(@Lazy DatasourceService datasourceService, MetaDataBackend metaDataBackend) {
        this.datasourceService = datasourceService;
        this.metaDataBackend = metaDataBackend;
    }

    @Override
    public List<DatabaseDTO> listAllDatabase(Long workspaceId) {

        List<DatasourceDTO> datasourceDTOS = datasourceService.listAll(workspaceId, null);
        return datasourceDTOS.stream().map(datasourceDTO -> {
            DatabaseDTO databaseDTO = new DatabaseDTO();
            databaseDTO.setWorkspaceId(datasourceDTO.getWorkspaceId());
            databaseDTO.setEngine(datasourceDTO.getEngine());
            databaseDTO.setDatasourceName(datasourceDTO.getName());
            databaseDTO.setDbName(datasourceDTO.getDatabase());
            return databaseDTO;
        }).collect(Collectors.toList());
    }

    @Override
    public List<TableSchemaDTO> listAllTable(DatabaseDTO databaseDTO) {

        DatasourceDTO datasourceDTO = datasourceService.getOneByWorkspaceAndName(databaseDTO.getWorkspaceId(), databaseDTO.getDatasourceName());

        List<TableSchemaDTO> tableSchemas = metaDataBackend.getTableSchemas(datasourceDTO);
        return tableSchemas;
    }

    @Override
    public TableSchemaDTO getTableSchema(TableSchemaDTO tableSchemaDTO) {
        DatasourceDTO datasourceDTO = datasourceService.getOneByWorkspaceAndName(tableSchemaDTO.getWorkspaceId(), tableSchemaDTO.getDatasourceName());
        return metaDataBackend.getTableSchema(datasourceDTO, tableSchemaDTO.getTableName());
    }

    @Override
    public ConnectionParamDTO getDatasetConnection(DatasetDTO datasetDTO) {

        DatasetMetaConfigDTO config = datasetDTO.getConfig();
        DatasourceDTO datasource = datasourceService.getOneByWorkspaceAndName(datasetDTO.getWorkspaceId(), config.getDatasourceName());

        ConnectionParamDTO param = new ConnectionParamDTO();

        BeanUtils.copyProperties(datasource, param);
        return param;
    }

    @Override
    public TablePartition getLatestPartition(DatasetDTO datasetDTO) {
        return null;
    }

    @Override
    public List<TablePartition> getLastTenPartition(DatasetDTO datasetDTO) {
        return Lists.newArrayList();
    }
}
