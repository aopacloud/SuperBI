package net.aopacloud.superbi.model.converter;

import net.aopacloud.superbi.model.dto.SysRoleDTO;
import net.aopacloud.superbi.model.entity.SysRole;
import net.aopacloud.superbi.model.vo.SysRoleVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * @Author shinnie
 * @Description
 * @Date 10:48 2023/10/11
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface SysRoleConverter extends BaseConverter<SysRoleVO, SysRoleDTO, SysRole> {

}
