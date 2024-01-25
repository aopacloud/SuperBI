package net.aopacloud.superbi.model.converter;

import net.aopacloud.superbi.model.dto.DatasourceDTO;
import net.aopacloud.superbi.model.entity.Datasource;
import net.aopacloud.superbi.model.vo.DatasourceVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * @Author shinnie
 * @Description
 * @Date 18:14 2023/8/25
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface DatasourceConverter extends BaseConverter<DatasourceVO, DatasourceDTO, Datasource> {



}
