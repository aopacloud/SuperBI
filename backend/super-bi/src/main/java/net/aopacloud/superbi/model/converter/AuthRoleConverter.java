package net.aopacloud.superbi.model.converter;

import net.aopacloud.superbi.model.dto.AuthRoleDTO;
import net.aopacloud.superbi.model.entity.AuthRole;
import net.aopacloud.superbi.model.vo.AuthRoleVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * @Author shinnie
 * @Description
 * @Date 15:48 2023/8/17
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface AuthRoleConverter extends BaseConverter<AuthRoleVO, AuthRoleDTO, AuthRole> {


}
