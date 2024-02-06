package net.aopacloud.superbi.mapper;


import net.aopacloud.superbi.model.entity.DatasetField;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author: hu.dong
 * @date: 2021/10/20
 **/
public interface DatasetFieldMapper {

    DatasetField selectById(Long id);

    List<DatasetField> selectByDatasetAndVersion(@Param("datasetId") Long datasetId, @Param("version") Integer version);

    int insert(DatasetField datasetField);

}
