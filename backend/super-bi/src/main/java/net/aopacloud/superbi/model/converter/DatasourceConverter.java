package net.aopacloud.superbi.model.converter;

import net.aopacloud.superbi.model.dto.DatasourceDTO;
import net.aopacloud.superbi.model.entity.Datasource;
import net.aopacloud.superbi.model.vo.DatasourceVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * @Author shinnie
 * @Description
 * @Date 18:14 2023/8/25
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface DatasourceConverter extends BaseConverter<DatasourceVO, DatasourceDTO, Datasource> {

    @Override
    @Mapping(target = "password", constant = "*****")
    DatasourceVO toVO(DatasourceDTO datasourceDTO);

    @Override
    @Mapping(target = "password", constant = "*****")
    List<DatasourceVO> toVOList(List<DatasourceDTO> datasourceDTOS);
}
