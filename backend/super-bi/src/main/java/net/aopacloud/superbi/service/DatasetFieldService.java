package net.aopacloud.superbi.service;

import net.aopacloud.superbi.model.vo.DatasetFieldCheckResultVO;
import net.aopacloud.superbi.model.vo.DatasetFieldCheckVO;

/**
 * @author: hudong
 * @date: 2023/10/25
 * @description:
 */
public interface DatasetFieldService {
    DatasetFieldCheckResultVO check(DatasetFieldCheckVO datasetFieldCheck);
}
