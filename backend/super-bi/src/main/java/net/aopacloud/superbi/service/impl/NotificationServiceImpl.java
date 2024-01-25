package net.aopacloud.superbi.service.impl;

import net.aopacloud.superbi.common.core.context.SecurityContextHolder;
import net.aopacloud.superbi.common.core.utils.JsonUtils;
import net.aopacloud.superbi.enums.NotificationTypeEnum;
import net.aopacloud.superbi.enums.PositionEnum;
import net.aopacloud.superbi.mapper.NotificationMapper;
import net.aopacloud.superbi.model.converter.NotificationConverter;
import net.aopacloud.superbi.model.dto.ApprovalNoticeDTO;
import net.aopacloud.superbi.model.dto.NotificationDTO;
import net.aopacloud.superbi.model.dto.PermissionNoticeDTO;
import net.aopacloud.superbi.model.entity.Notification;
import net.aopacloud.superbi.model.query.NoticeQuery;
import net.aopacloud.superbi.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author shinnie
 * @Description
 * @Date 10:58 2023/9/25
 */
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;

    private final NotificationConverter notificationConverter;

    @Override
    public Notification permissionNotice(String toUser, PositionEnum resourceType,
                                         Long resourceId, String resourceName, PermissionNoticeDTO noticeDTO) {
        Notification notification = Notification.builder()
                .username(toUser)
                .readed(0)
                .type(NotificationTypeEnum.PERMISSION.name())
                .resourceType(resourceType.getName())
                .resourceId(resourceId)
                .resourceName(resourceName)
                .content(JsonUtils.toJsonString(noticeDTO)).build();
        notificationMapper.insert(notification);
        return notification;
    }

    @Override
    public Notification approvalNotice(String toUser, PositionEnum resourceType,
                                       Long resourceId, String resourceName, ApprovalNoticeDTO noticeDTO) {
        Notification notification = Notification.builder()
                .username(toUser)
                .readed(0)
                .type(NotificationTypeEnum.APPROVAL.name())
                .resourceType(resourceType.getName())
                .resourceId(resourceId)
                .resourceName(resourceName)
                .content(JsonUtils.toJsonString(noticeDTO)).build();
        notificationMapper.insert(notification);
        return notification;
    }

    @Override
    public Notification accountNotice(String toUser, String content) {
        Notification notification = Notification.builder()
                .username(toUser)
                .readed(0)
                .type(NotificationTypeEnum.ACCOUNT.name())
                .content(content).build();
        notificationMapper.insert(notification);
        return notification;
    }

    @Override
    public List<NotificationDTO> query(NoticeQuery noticeQuery) {
        noticeQuery.setUsername(SecurityContextHolder.getUserName());
        List<NotificationDTO> list = notificationMapper.search(noticeQuery);
        return list;
    }

    @Override
    public void readNotice(List<Integer> idList) {
        int i = notificationMapper.readNotice(idList);
    }

    @Override
    public Integer unreadCount() {
        return notificationMapper.unreadCount(SecurityContextHolder.getUserName());
    }

}
