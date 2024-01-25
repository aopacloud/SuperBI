package net.aopacloud.superbi.model.converter;

import net.aopacloud.superbi.model.dto.DatasetFieldDTO;
import net.aopacloud.superbi.model.entity.DatasetField;
import net.aopacloud.superbi.model.vo.DatasetFieldVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface DatasetFieldConverter extends BaseConverter<DatasetFieldVO, DatasetFieldDTO, DatasetField> {
}
