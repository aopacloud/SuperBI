package net.aopacloud.superbi.model.converter;

import net.aopacloud.superbi.model.dto.DatabaseDTO;
import net.aopacloud.superbi.model.vo.DatabaseVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * @author: hudong
 * @date: 2023/10/23
 * @description:
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface DatabaseConverter {

    DatabaseDTO toDTO(DatabaseVO databaseVO);

    DatabaseVO toVO(DatabaseDTO databaseDTO);

    List<DatabaseVO> toVOList(List<DatabaseDTO> databaseDTOS);

}
