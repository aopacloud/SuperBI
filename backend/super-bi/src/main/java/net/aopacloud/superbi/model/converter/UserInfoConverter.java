package net.aopacloud.superbi.model.converter;

import net.aopacloud.superbi.model.dto.UserInfoDTO;
import net.aopacloud.superbi.model.vo.UserInfoVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring" , uses = {WorkspaceConverter.class} )
public interface UserInfoConverter extends BaseConverter<UserInfoVO, UserInfoDTO, Object>{
}
