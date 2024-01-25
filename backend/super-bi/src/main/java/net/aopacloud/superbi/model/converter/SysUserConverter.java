package net.aopacloud.superbi.model.converter;

import net.aopacloud.superbi.model.dto.SysUserDTO;
import net.aopacloud.superbi.model.entity.SysUser;
import net.aopacloud.superbi.model.vo.SysUserVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface SysUserConverter extends BaseConverter<SysUserVO, SysUserDTO, SysUser>{
}
