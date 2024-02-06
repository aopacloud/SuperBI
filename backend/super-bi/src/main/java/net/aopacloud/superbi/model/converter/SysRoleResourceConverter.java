package net.aopacloud.superbi.model.converter;

import net.aopacloud.superbi.model.dto.SysRoleResourceDTO;
import net.aopacloud.superbi.model.entity.SysRoleResource;
import net.aopacloud.superbi.model.vo.SysRoleResourceVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * @Author shinnie
 * @Description
 * @Date 10:48 2023/10/11
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface SysRoleResourceConverter extends BaseConverter<SysRoleResourceVO, SysRoleResourceDTO, SysRoleResource> {

    @Override
    @Mapping(target = "resourceCode", expression = "java(sysRoleResourceDTO.getResourceCode().stream().collect(java.util.stream.Collectors.joining(\",\")))")
    SysRoleResource toEntity(SysRoleResourceDTO sysRoleResourceDTO);

    @Override
    @Mapping(target = "resourceCode", expression = "java(com.google.common.collect.Sets.newHashSet(sysRoleResource.getResourceCode().split(\",\")))")
    SysRoleResourceDTO entityToDTO(SysRoleResource sysRoleResource);

}
