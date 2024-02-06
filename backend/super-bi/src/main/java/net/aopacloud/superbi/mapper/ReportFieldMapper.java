package net.aopacloud.superbi.mapper;

import net.aopacloud.superbi.model.dto.ReportFieldDTO;
import net.aopacloud.superbi.model.entity.ReportField;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ReportFieldMapper {

    List<ReportFieldDTO> selectByReportAndCategory(@Param("reportId") Long reportId, @Param("category") String category);

    void deleteByReport(@Param("reportId") Long reportId);

    int insert(ReportField reportField);

}