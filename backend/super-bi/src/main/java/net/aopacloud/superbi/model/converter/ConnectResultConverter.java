package net.aopacloud.superbi.model.converter;

import net.aopacloud.superbi.model.dto.ConnectResultDTO;
import net.aopacloud.superbi.model.vo.ConnectResultVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;


@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface ConnectResultConverter extends DTOVOConverter<ConnectResultVO, ConnectResultDTO> {
}
