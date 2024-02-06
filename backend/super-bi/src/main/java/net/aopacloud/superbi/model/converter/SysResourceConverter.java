package net.aopacloud.superbi.model.converter;

import net.aopacloud.superbi.model.dto.SysResourceDTO;
import net.aopacloud.superbi.model.entity.SysResource;
import net.aopacloud.superbi.model.vo.SysResourceVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * @Author shinnie
 * @Description
 * @Date 10:48 2023/10/11
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface SysResourceConverter extends BaseConverter<SysResourceVO, SysResourceDTO, SysResource> {

}
