package net.aopacloud.superbi.service;

import net.aopacloud.superbi.model.dto.*;

import java.util.List;

/**
 * @Author shinnie
 * @Description
 * @Date 17:23 2023/8/25
 */
public interface MetaDataService {

    /**
     * get all database
     *
     * @param workspaceId
     * @return
     */
    List<DatabaseDTO> listAllDatabase(Long workspaceId);

    /**
     * get all table
     *
     * @param databaseDTO
     * @return
     */
    List<TableSchemaDTO> listAllTable(DatabaseDTO databaseDTO);


    /**
     * get table detail with fields
     *
     * @param tableSchemaDTO
     * @return
     */
    TableSchemaDTO getTableSchema(TableSchemaDTO tableSchemaDTO);


    /**
     * get dataset connection
     *
     * @param datasetDTO
     * @return
     */
    ConnectionParamDTO getDatasetConnection(DatasetDTO datasetDTO);

    /**
     * get origin table that dataset used latest one partition
     *
     * @param datasetDTO
     * @return
     */
    TablePartition getLatestPartition(DatasetDTO datasetDTO);

    /**
     * get origin table that dataset used latest ten partition
     *
     * @param datasetDTO
     * @return
     */
    List<TablePartition> getLastTenPartition(DatasetDTO datasetDTO);
}
