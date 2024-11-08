package net.aopacloud.superbi.mapper;

import net.aopacloud.superbi.model.dto.ReportDTO;
import net.aopacloud.superbi.model.entity.Report;
import net.aopacloud.superbi.model.query.RecycleQuery;
import net.aopacloud.superbi.model.query.ReportQuery;
import net.aopacloud.superbi.model.vo.RecycleVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ReportMapper {

    Report selectById(@Param("id") Long id);

    List<Report> selectAll();

    Report selectByIdRecycle(@Param("id") Long id);

    List<ReportDTO> search(ReportQuery query);

    List<RecycleVO> searchByRecycle(RecycleQuery query);

    int insert(Report report);

    int update(Report report);

    int deleteById(@Param("id") Long id);

    int recycleDelete(@Param("id") Long id);

    int restore(@Param("id") Long id);

    Integer countByUsername(@Param("username") String username);

    void updateCreator(@Param("fromUsername") String fromUsername, @Param("toUsername") String toUsername);

    void updateCreatorById(@Param("id") Long id, @Param("fromUsername") String fromUsername, @Param("toUsername") String toUsername);

    void updateOperatorById(@Param("id") Long id, @Param("operator") String operator);

    List<ReportDTO> selectByDatasetId(@Param("datasetId") Long datasetId);

    List<ReportDTO> selectByDatasetRecycle(@Param("datasetId") Long datasetId);
}