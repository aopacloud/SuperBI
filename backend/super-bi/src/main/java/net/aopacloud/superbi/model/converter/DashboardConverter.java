package net.aopacloud.superbi.model.converter;

import net.aopacloud.superbi.model.dto.DashboardDTO;
import net.aopacloud.superbi.model.entity.Dashboard;
import net.aopacloud.superbi.model.vo.DashboardVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * @Author shinnie
 * @Description
 * @Date 16:14 2023/8/30
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface DashboardConverter extends BaseConverter<DashboardVO, DashboardDTO, Dashboard> {
}
