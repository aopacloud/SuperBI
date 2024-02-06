package net.aopacloud.superbi.model.converter;

import net.aopacloud.superbi.model.dto.DatasetQueryLogDTO;
import net.aopacloud.superbi.model.entity.DatasetQueryLog;
import net.aopacloud.superbi.model.vo.DatasetQueryLogVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface DatasetQueryLogConverter extends BaseConverter<DatasetQueryLogVO, DatasetQueryLogDTO, DatasetQueryLog> {
}
