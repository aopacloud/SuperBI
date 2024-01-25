package net.aopacloud.superbi.mapper;

import net.aopacloud.superbi.model.dto.DatasetApplyDTO;
import net.aopacloud.superbi.model.entity.DatasetApply;
import net.aopacloud.superbi.model.query.DatasetApplyQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DatasetApplyMapper {
    List<DatasetApplyDTO> search(DatasetApplyQuery query);

    DatasetApply selectOneById(Long id);

    void updateApply(DatasetApply datasetApply);

    void save(DatasetApply datasetApply);

    List<DatasetApplyDTO> selectByDatasetAndUsername(@Param("datasetId") Long datasetId,@Param("username") String username);

    int selectApplyingCount(@Param("username") String username);

    int selectReviewCount(@Param("username") String username);

    int selectOperationCount(@Param("username") String username);

    List<DatasetApplyDTO> selectUnFinished();
}
