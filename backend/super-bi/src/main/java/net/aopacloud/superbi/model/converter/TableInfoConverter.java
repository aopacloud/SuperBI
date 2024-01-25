package net.aopacloud.superbi.model.converter;

import net.aopacloud.superbi.model.dto.TableInfoDTO;
import net.aopacloud.superbi.model.vo.TableInfoVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface TableInfoConverter extends DTOVOConverter<TableInfoVO, TableInfoDTO> {
}
