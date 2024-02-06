package net.aopacloud.superbi.model.converter;

import net.aopacloud.superbi.model.dto.DashboardComponentDTO;
import net.aopacloud.superbi.model.entity.DashboardComponent;
import net.aopacloud.superbi.model.vo.DashboardComponentVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * @Author shinnie
 * @Description
 * @Date 17:03 2023/8/30
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface DashboardComponentConverter extends BaseConverter<DashboardComponentVO, DashboardComponentDTO, DashboardComponent> {
}
