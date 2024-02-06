package net.aopacloud.superbi.model.converter;

import net.aopacloud.superbi.model.dto.TableSchemaDTO;
import net.aopacloud.superbi.model.vo.TableSchemaVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * @author: hudong
 * @date: 2023/10/23
 * @description:
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface TableConverter extends DTOVOConverter<TableSchemaVO, TableSchemaDTO> {
}
