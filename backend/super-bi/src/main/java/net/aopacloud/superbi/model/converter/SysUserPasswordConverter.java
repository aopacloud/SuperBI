package net.aopacloud.superbi.model.converter;

import net.aopacloud.superbi.model.dto.SysUserDTO;
import net.aopacloud.superbi.model.vo.SysUserPasswordVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/12/8 14:25
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface SysUserPasswordConverter extends DTOVOConverter<SysUserPasswordVO, SysUserDTO> {
}
