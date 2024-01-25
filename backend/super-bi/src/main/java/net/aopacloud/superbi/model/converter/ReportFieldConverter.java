package net.aopacloud.superbi.model.converter;

import net.aopacloud.superbi.model.dto.ReportFieldDTO;
import net.aopacloud.superbi.model.entity.ReportField;
import net.aopacloud.superbi.model.vo.ReportFieldVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * @Author shinnie
 * @Description
 * @Date 15:42 2023/8/30
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface ReportFieldConverter extends BaseConverter<ReportFieldVO, ReportFieldDTO, ReportField> {
}
