package net.aopacloud.superbi.mapper;

import net.aopacloud.superbi.model.entity.DatasetMetaConfig;
import org.apache.ibatis.annotations.Param;

public interface DatasetMetaConfigMapper {
    DatasetMetaConfig selectOneByDatasetAndVersion(@Param("datasetId") Long datasetId, @Param("version") Integer version);

    void insert(DatasetMetaConfig config);
}
