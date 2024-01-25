package net.aopacloud.superbi.mapper;

import net.aopacloud.superbi.model.dto.ReportDTO;
import net.aopacloud.superbi.model.entity.Report;
import net.aopacloud.superbi.model.query.ReportQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ReportMapper {

    Report selectById(@Param("id") Long id);

    List<ReportDTO> search(ReportQuery query);

    int insert(Report report);

    int update(Report report);

    int deleteById(@Param("id") Long id);

    Integer countByUsername(@Param("username") String username);

    void updateCreator(@Param("fromUsername") String fromUsername, @Param("toUsername") String toUsername);
}