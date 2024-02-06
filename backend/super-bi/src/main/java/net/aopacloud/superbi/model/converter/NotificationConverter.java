package net.aopacloud.superbi.model.converter;

import net.aopacloud.superbi.model.dto.NotificationDTO;
import net.aopacloud.superbi.model.entity.Notification;
import net.aopacloud.superbi.model.vo.NotificationVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * @Author shinnie
 * @Description
 * @Date 17:15 2023/9/25
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface NotificationConverter extends BaseConverter<NotificationVO, NotificationDTO, Notification> {
}
