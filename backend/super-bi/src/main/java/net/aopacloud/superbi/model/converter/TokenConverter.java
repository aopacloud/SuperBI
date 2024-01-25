package net.aopacloud.superbi.model.converter;

import net.aopacloud.superbi.model.dto.TokenDTO;
import net.aopacloud.superbi.model.vo.TokenVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/12/6 12:16
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface TokenConverter extends DTOVOConverter<TokenVO, TokenDTO> {
}
