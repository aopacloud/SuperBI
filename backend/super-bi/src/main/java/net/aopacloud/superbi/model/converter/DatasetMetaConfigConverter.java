package net.aopacloud.superbi.model.converter;

import net.aopacloud.superbi.model.dto.DatasetMetaConfigDTO;
import net.aopacloud.superbi.model.entity.DatasetMetaConfig;
import net.aopacloud.superbi.model.vo.DatasetMetaConfigVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface DatasetMetaConfigConverter extends BaseConverter<DatasetMetaConfigVO, DatasetMetaConfigDTO, DatasetMetaConfig> {
}
