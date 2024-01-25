package net.aopacloud.superbi.model.converter;

import net.aopacloud.superbi.model.dto.WorkspaceUserResourceDTO;
import net.aopacloud.superbi.model.vo.WorkspaceUserResourceVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface WorkspaceUserResourceConverter {

    WorkspaceUserResourceVO toVO(WorkspaceUserResourceDTO dto);

}
