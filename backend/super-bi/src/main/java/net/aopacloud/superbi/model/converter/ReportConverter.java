package net.aopacloud.superbi.model.converter;

import net.aopacloud.superbi.model.dto.ReportDTO;
import net.aopacloud.superbi.model.entity.Report;
import net.aopacloud.superbi.model.vo.ReportVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * @Author shinnie
 * @Description
 * @Date 11:35 2023/8/30
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface ReportConverter extends BaseConverter<ReportVO, ReportDTO, Report> {

}
