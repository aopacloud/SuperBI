package net.aopacloud.superbi.model.converter;

import net.aopacloud.superbi.model.dto.DatasetMetaConfigDTO;
import net.aopacloud.superbi.model.entity.DatasetMetaConfig;
import net.aopacloud.superbi.model.vo.DatasetMetaConfigVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface DatasetMetaConfigConverter extends BaseConverter<DatasetMetaConfigVO, DatasetMetaConfigDTO, DatasetMetaConfig> {

    @Override
    @Mapping(target = "content", expression = "java(net.aopacloud.superbi.util.JSONUtils.toJsonString(dto.getContent()))")
    DatasetMetaConfig toEntity(DatasetMetaConfigDTO dto);

    @Override
    @Mapping(target="content" , expression = "java(net.aopacloud.superbi.util.JSONUtils.parseObject(entity.getContent(), net.aopacloud.superbi.model.domain.MetaConfigContent.class))")
    DatasetMetaConfigDTO entityToDTO(DatasetMetaConfig entity);
}
