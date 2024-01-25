package net.aopacloud.superbi.service.impl;

import net.aopacloud.superbi.common.core.context.SecurityContextHolder;
import net.aopacloud.superbi.mapper.DatasetQueryLogMapper;
import net.aopacloud.superbi.model.converter.DatasetQueryLogConverter;
import net.aopacloud.superbi.model.dto.DatasetQueryLogDTO;
import net.aopacloud.superbi.model.entity.DatasetQueryLog;
import net.aopacloud.superbi.service.DatasetQueryLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: hudong
 * @date: 2023/9/8
 * @description:
 */
@Service
@RequiredArgsConstructor
public class DatasetQueryLogServiceImpl implements DatasetQueryLogService {

    private final DatasetQueryLogMapper datasetQueryLogMapper;

    private final DatasetQueryLogConverter converter;

    @Override
    public void saveQueryLog(DatasetQueryLogDTO datasetQueryLogDTO) {

        DatasetQueryLog datasetQueryLog = converter.toEntity(datasetQueryLogDTO);

        datasetQueryLogMapper.save(datasetQueryLog);
    }

    @Override
    public List<DatasetQueryLogDTO> findByDataset(Long datasetId) {
        return datasetQueryLogMapper.selectByDatasetAndUsername(datasetId, SecurityContextHolder.getUserName());
    }

    @Override
    public DatasetQueryLogDTO findOne(Long id) {
        return datasetQueryLogMapper.selectById(id);
    }
}
