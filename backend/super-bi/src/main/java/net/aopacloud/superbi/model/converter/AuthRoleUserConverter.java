package net.aopacloud.superbi.model.converter;

import net.aopacloud.superbi.model.dto.AuthRoleUserDTO;
import net.aopacloud.superbi.model.entity.AuthRoleUser;
import net.aopacloud.superbi.model.vo.AuthRoleUserVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface AuthRoleUserConverter extends BaseConverter<AuthRoleUserVO, AuthRoleUserDTO, AuthRoleUser> {
}
