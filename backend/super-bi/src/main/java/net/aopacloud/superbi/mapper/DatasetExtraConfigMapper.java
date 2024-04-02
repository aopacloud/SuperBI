package net.aopacloud.superbi.mapper;

import net.aopacloud.superbi.model.entity.DatasetExtraConfig;
import net.aopacloud.superbi.model.entity.DatasetMetaConfig;
import org.apache.ibatis.annotations.Param;

public interface DatasetExtraConfigMapper {
    DatasetExtraConfig selectOneByDatasetAndVersion(@Param("datasetId") Long datasetId, @Param("version") Integer version);

    void insert(DatasetExtraConfig config);
}
