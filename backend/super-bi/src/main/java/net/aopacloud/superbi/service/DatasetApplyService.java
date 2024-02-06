package net.aopacloud.superbi.service;

import net.aopacloud.superbi.model.dto.ApplyMessageDTO;
import net.aopacloud.superbi.model.dto.DatasetApplyDTO;
import net.aopacloud.superbi.model.query.DatasetApplyQuery;

import java.util.List;

/**
 * @author: hudong
 * @date: 2023/9/6
 * @description:
 */
public interface DatasetApplyService {
    /**
     * get apply list
     *
     * @param query
     * @return
     */
    List<DatasetApplyDTO> search(DatasetApplyQuery query);

    /**
     * get specified id apply
     *
     * @param id
     * @return
     */
    DatasetApplyDTO findOne(Long id);

    /**
     * reject apply
     * reviewer can reject apply
     *
     * @param datasetApplyDTO
     * @return
     */
    DatasetApplyDTO rejectApply(DatasetApplyDTO datasetApplyDTO);

    /**
     * revoke apply
     * applicant can revoke apply
     *
     * @param id
     * @return
     */
    DatasetApplyDTO revokeApply(Long id);

    /**
     * update apply
     *
     * @param datasetApplyDTO
     * @return
     */
    DatasetApplyDTO updateApply(DatasetApplyDTO datasetApplyDTO);

    /**
     * post apply
     *
     * @param datasetApplyDTO
     * @return
     */
    DatasetApplyDTO postApply(DatasetApplyDTO datasetApplyDTO);

    /**
     * pass apply
     *
     * @param id
     * @return
     */
    DatasetApplyDTO passApply(Long id);

    /**
     * get apply list by specified dataset and username
     *
     * @param datasetId
     * @param username
     * @return
     */
    List<DatasetApplyDTO> findApplyByDatasetAndUsername(Long datasetId, String username);

    /**
     * get apply message
     *
     * @return
     */
    ApplyMessageDTO getApplyMessage();

    /**
     * get unfinished apply
     *
     * @return
     */
    List<DatasetApplyDTO> findUnFinished();

    /**
     * the dataset is applying
     *
     * @param datasetId
     * @return
     */
    boolean isApplying(Long datasetId);

    /**
     * disagree apply
     *
     * @param id
     * @return
     */
    DatasetApplyDTO disagree(DatasetApplyDTO datasetApplyDTO);
}
