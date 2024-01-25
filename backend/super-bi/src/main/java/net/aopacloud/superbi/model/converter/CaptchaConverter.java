package net.aopacloud.superbi.model.converter;

import net.aopacloud.superbi.model.dto.CaptchaDTO;
import net.aopacloud.superbi.model.vo.CaptchaVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/12/6 12:22
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface CaptchaConverter extends DTOVOConverter<CaptchaVO, CaptchaDTO> {
}
