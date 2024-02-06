package net.aopacloud.superbi.model.converter;

import net.aopacloud.superbi.model.dto.WorkspaceConfigDTO;
import net.aopacloud.superbi.model.entity.WorkspaceConfig;
import net.aopacloud.superbi.model.vo.WorkspaceConfigVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface WorkspaceConfigConverter extends BaseConverter<WorkspaceConfigVO, WorkspaceConfigDTO, WorkspaceConfig> {
}
