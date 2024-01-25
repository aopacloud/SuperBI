package net.aopacloud.superbi.model.converter;

import net.aopacloud.superbi.model.dto.SysMenuDTO;
import net.aopacloud.superbi.model.entity.SysMenu;
import net.aopacloud.superbi.model.vo.SysMenuVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface SysMenuConverter extends BaseConverter<SysMenuVO, SysMenuDTO, SysMenu>{
}
