package net.aopacloud.superbi.service;

import net.aopacloud.superbi.enums.PositionEnum;
import net.aopacloud.superbi.model.dto.ApprovalNoticeDTO;
import net.aopacloud.superbi.model.dto.NotificationDTO;
import net.aopacloud.superbi.model.dto.PermissionNoticeDTO;
import net.aopacloud.superbi.model.entity.Notification;
import net.aopacloud.superbi.model.query.NoticeQuery;

import java.util.List;

/**
 * @Author shinnie
 * @Description
 * @Date 10:58 2023/9/25
 */
public interface NotificationService {

    List<NotificationDTO> query(NoticeQuery noticeQuery);

    Notification permissionNotice(String toUser, PositionEnum resourceType,
                                  Long resourceId, String resourceName, PermissionNoticeDTO noticeDTO);

    Notification approvalNotice(String toUser, PositionEnum resourceType,
                                Long resourceId, String resourceName, ApprovalNoticeDTO noticeDTO);

    Notification accountNotice(String toUser, String content);

    void readNotice(List<Integer> idList);

    Integer unreadCount();
}
