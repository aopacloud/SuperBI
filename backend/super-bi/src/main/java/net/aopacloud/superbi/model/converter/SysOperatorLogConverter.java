package net.aopacloud.superbi.model.converter;

import net.aopacloud.superbi.model.dto.SysOperatorLogDTO;
import net.aopacloud.superbi.model.entity.SysOperatorLog;
import net.aopacloud.superbi.model.vo.SysOperatorLogVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/11/28 18:15
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface SysOperatorLogConverter extends BaseConverter<SysOperatorLogVO, SysOperatorLogDTO, SysOperatorLog> {
}
