package net.aopacloud.superbi.model.converter;

import net.aopacloud.superbi.model.dto.SysUserDTO;
import net.aopacloud.superbi.model.dto.WorkspaceDTO;
import net.aopacloud.superbi.model.entity.Workspace;
import net.aopacloud.superbi.model.entity.WorkspaceConfig;
import net.aopacloud.superbi.model.vo.WorkspaceVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring", uses = {SysUserConverter.class, WorkspaceConfigConverter.class})
public interface WorkspaceConverter extends BaseConverter<WorkspaceVO, WorkspaceDTO, Workspace>{

    @Mapping(source = "config", target = "config")
    @Mapping(source = "admins" , target = "admins")
    @Mapping(source = "memberCount" , target = "memberCount")
    @Mapping(source = "roleCount" , target = "roleCount")
    @Mapping(source = "workspace.id" , target = "id")
    @Mapping(source = "workspace.createTime" , target = "createTime")
    @Mapping(source = "workspace.updateTime" , target = "updateTime")
    WorkspaceDTO toDTO(Workspace workspace, WorkspaceConfig config, List<SysUserDTO> admins, Integer memberCount, Integer roleCount);

}
