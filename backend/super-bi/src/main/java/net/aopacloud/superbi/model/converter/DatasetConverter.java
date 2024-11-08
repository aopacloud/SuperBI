package net.aopacloud.superbi.model.converter;

import net.aopacloud.superbi.model.dto.DatasetDTO;
import net.aopacloud.superbi.model.dto.DatasetMetaConfigDTO;
import net.aopacloud.superbi.model.entity.Dataset;
import net.aopacloud.superbi.model.entity.DatasetField;
import net.aopacloud.superbi.model.entity.DatasetMetaConfig;
import net.aopacloud.superbi.model.vo.DatasetVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring", uses = {DatasetFieldConverter.class, DatasetMetaConfigConverter.class})
public interface DatasetConverter extends BaseConverter<DatasetVO, DatasetDTO, Dataset> {

    @Mapping(source = "config", target = "config")
    @Mapping(source = "fields", target = "fields")
    @Mapping(source = "dataset.id", target = "id")
    @Mapping(source = "dataset.version", target = "version")
    @Mapping(source = "dataset.createTime", target = "createTime")
    @Mapping(source = "dataset.updateTime", target = "updateTime")
    DatasetDTO toDTO(Dataset dataset, DatasetMetaConfigDTO config, List<DatasetField> fields);
}
