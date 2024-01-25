package net.aopacloud.superbi.model.converter;

import net.aopacloud.superbi.model.dto.WorkspaceMemberDTO;
import net.aopacloud.superbi.model.entity.WorkspaceMember;
import net.aopacloud.superbi.model.vo.WorkspaceMemberVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface WorkspaceMemberConverter extends BaseConverter<WorkspaceMemberVO, WorkspaceMemberDTO, WorkspaceMember>{
}
