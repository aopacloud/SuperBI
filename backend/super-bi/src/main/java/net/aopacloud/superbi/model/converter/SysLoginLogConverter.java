package net.aopacloud.superbi.model.converter;

import net.aopacloud.superbi.model.dto.SysLoginLogDTO;
import net.aopacloud.superbi.model.entity.SysLoginLog;
import net.aopacloud.superbi.model.vo.SysLoginLogVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/11/28 18:21
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface SysLoginLogConverter extends BaseConverter<SysLoginLogVO, SysLoginLogDTO, SysLoginLog> {
}
