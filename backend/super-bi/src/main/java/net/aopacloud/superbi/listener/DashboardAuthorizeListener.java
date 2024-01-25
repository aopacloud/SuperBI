package net.aopacloud.superbi.listener;


import net.aopacloud.superbi.common.core.context.SecurityContextHolder;
import net.aopacloud.superbi.enums.AuthorizeStatusEnum;
import net.aopacloud.superbi.enums.PositionEnum;
import net.aopacloud.superbi.listener.event.DashboardAuthorizeUpdateEvent;
import net.aopacloud.superbi.model.dto.DashboardDTO;
import net.aopacloud.superbi.model.dto.PermissionNoticeDTO;
import net.aopacloud.superbi.service.DashboardService;
import net.aopacloud.superbi.service.NotificationService;
import net.aopacloud.superbi.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DashboardAuthorizeListener {

    private final DashboardService dashboardService;

    private final NotificationService notificationService;

    private final SysUserService sysUserService;

    @EventListener
    public void onDashboardAuthorizeModify(DashboardAuthorizeUpdateEvent event) {
        log.info("receive event {}", event);
        try {
            DashboardDTO dashboardDTO = dashboardService.findOne(event.getDashboardId());
            PermissionNoticeDTO permissionNoticeDTO = PermissionNoticeDTO.builder()
                    .status(AuthorizeStatusEnum.AUTHORIZED)
                    .operator(sysUserService.getUserAliasName(SecurityContextHolder.getUserName()))
                    .build();
            notificationService.permissionNotice(event.getUsername(), PositionEnum.DASHBOARD, event.getDashboardId(), dashboardDTO.getName(), permissionNoticeDTO);
        } catch (Exception e) {
            log.error("update dashboard authorize status error", e);
        }
    }

}
