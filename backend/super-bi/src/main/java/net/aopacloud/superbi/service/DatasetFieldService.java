package net.aopacloud.superbi.service;

import net.aopacloud.superbi.model.dto.DatasetFieldDTO;
import net.aopacloud.superbi.model.vo.DatasetFieldCheckResultVO;
import net.aopacloud.superbi.model.vo.DatasetFieldCheckVO;

import java.util.List;
import java.util.Set;

/**
 * @author: hudong
 * @date: 2023/10/25
 * @description:
 */
public interface DatasetFieldService {
    DatasetFieldCheckResultVO check(DatasetFieldCheckVO datasetFieldCheck);

    List<DatasetFieldDTO> findIntersectionFields(Set<Long> datasetIds);
}
