package net.aopacloud.superbi.model.converter;

import net.aopacloud.superbi.model.dto.SysRoleMenuDTO;
import net.aopacloud.superbi.model.entity.SysRoleMenu;
import net.aopacloud.superbi.model.vo.SysRoleMenuVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface SysRoleMenuConverter extends BaseConverter<SysRoleMenuVO, SysRoleMenuDTO, SysRoleMenu> {
}
