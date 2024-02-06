package net.aopacloud.superbi.mapper;

import net.aopacloud.superbi.model.dto.NotificationDTO;
import net.aopacloud.superbi.model.entity.Notification;
import net.aopacloud.superbi.model.query.NoticeQuery;

import java.util.List;

public interface NotificationMapper {

    int deleteById(Integer id);

    int insert(Notification row);

    Notification selectById(Integer id);

    List<NotificationDTO> search(NoticeQuery noticeQuery);

    int update(Notification row);

    int readNotice(List<Integer> list);

    Integer unreadCount(String username);
}