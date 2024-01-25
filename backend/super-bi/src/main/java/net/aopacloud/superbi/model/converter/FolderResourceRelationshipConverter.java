package net.aopacloud.superbi.model.converter;

import net.aopacloud.superbi.model.dto.FolderResourceRelationshipDTO;
import net.aopacloud.superbi.model.entity.FolderResourceRel;
import net.aopacloud.superbi.model.vo.FolderResourceRelationshipVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface FolderResourceRelationshipConverter extends BaseConverter<FolderResourceRelationshipVO, FolderResourceRelationshipDTO, FolderResourceRel> {


}
