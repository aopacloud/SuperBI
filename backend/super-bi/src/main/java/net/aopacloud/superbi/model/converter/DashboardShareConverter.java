package net.aopacloud.superbi.model.converter;

import net.aopacloud.superbi.model.dto.DashboardShareDTO;
import net.aopacloud.superbi.model.entity.DashboardShare;
import net.aopacloud.superbi.model.vo.DashboardShareVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * @Author shinnie
 * @Description
 * @Date 17:40 2023/9/1
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface DashboardShareConverter extends BaseConverter<DashboardShareVO, DashboardShareDTO, DashboardShare> {
}
