package net.aopacloud.superbi.model.converter;


import net.aopacloud.superbi.model.dto.ApplyMessageDTO;
import net.aopacloud.superbi.model.vo.ApplyMessageVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface ApplyMessageConverter extends DTOVOConverter<ApplyMessageVO, ApplyMessageDTO> {

}
