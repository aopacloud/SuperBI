package net.aopacloud.superbi.service;

import net.aopacloud.superbi.model.dto.DatasetQueryLogDTO;

import java.util.List;

/**
 * @author: hudong
 * @date: 2023/9/8
 * @description:
 */
public interface DatasetQueryLogService {

    void saveQueryLog(DatasetQueryLogDTO datasetQueryLogDTO);

    List<DatasetQueryLogDTO> findByDataset(Long datasetId);

    DatasetQueryLogDTO findOne(Long id);
}
