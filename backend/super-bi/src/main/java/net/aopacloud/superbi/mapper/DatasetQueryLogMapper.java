package net.aopacloud.superbi.mapper;

import net.aopacloud.superbi.model.dto.DatasetQueryLogDTO;
import net.aopacloud.superbi.model.entity.DatasetQueryLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DatasetQueryLogMapper {

    List<DatasetQueryLogDTO> selectByDatasetAndUsername(@Param("datasetId") Long datasetId, @Param("username") String username);

    void save(DatasetQueryLog datasetQueryLog);

    DatasetQueryLogDTO selectById(Long id);

    List<DatasetQueryLogDTO> selectByDatasetId(Long id);
}
