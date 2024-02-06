package net.aopacloud.superbi.model.converter;

import net.aopacloud.superbi.model.dto.DatasetApplyDTO;
import net.aopacloud.superbi.model.entity.DatasetApply;
import net.aopacloud.superbi.model.vo.DatasetApplyVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface DatasetApplyConverter extends BaseConverter<DatasetApplyVO, DatasetApplyDTO, DatasetApply> {
}
