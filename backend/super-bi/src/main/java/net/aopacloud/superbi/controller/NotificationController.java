package net.aopacloud.superbi.controller;

import net.aopacloud.superbi.common.core.utils.PageUtils;
import net.aopacloud.superbi.common.core.web.domain.RestApiResponse;
import net.aopacloud.superbi.common.core.web.page.PageVO;
import net.aopacloud.superbi.model.converter.NotificationConverter;
import net.aopacloud.superbi.model.dto.NotificationDTO;
import net.aopacloud.superbi.model.query.NoticeQuery;
import net.aopacloud.superbi.model.vo.NotificationVO;
import net.aopacloud.superbi.service.NotificationService;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Notification
 * @Author shinnie
 * @Description
 * @Date 10:58 2023/9/25
 */
@RestController
@RequestMapping("notice")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    private final NotificationConverter notificationConverter;

    /**
     * get latest message list
     * @param noticeQuery
     * @return
     */
    @CrossOrigin
    @GetMapping("")
    public RestApiResponse<PageVO<NotificationVO>> query(NoticeQuery noticeQuery) {
        PageUtils.startPage();
        List<NotificationDTO> list = notificationService.query(noticeQuery);
        PageInfo pageInfo = new PageInfo<>(list);
        PageVO pageVO = new PageVO(notificationConverter.toVOList(list), pageInfo.getTotal());
        return RestApiResponse.success(pageVO);
    }

    /**
     * mark message read
     * @param list
     * @return
     */
    @PostMapping("read")
    public RestApiResponse<String> read(@RequestBody List<Integer> list) {
        notificationService.readNotice(list);
        return RestApiResponse.success("success");
    }

    /**
     * get unread message count
     * @return
     */
    @GetMapping("/unreadCount")
    public RestApiResponse<Integer> unreadCount() {
        return RestApiResponse.success(notificationService.unreadCount());
    }
}
