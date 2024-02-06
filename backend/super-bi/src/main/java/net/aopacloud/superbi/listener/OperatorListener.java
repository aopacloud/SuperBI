package net.aopacloud.superbi.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.aopacloud.superbi.listener.event.OperatorEvent;
import net.aopacloud.superbi.service.SysOperatorLogService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @Description:
 * @Author: rick
 * @date: 2024/1/22 20:33
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OperatorListener {
    private final SysOperatorLogService sysOperatorLogService;

    @EventListener
    public void onOperatorLog(OperatorEvent operatorEvent) {
        log.info("receive event {}", operatorEvent);
        try {
            // TODO 完善用户登录信息，需要进一步测试
            // TODO 各个登录的地方加上日志
            sysOperatorLogService.insertOperatorLog(operatorEvent.getSysOperatorLog());
        } catch (Exception e) {
            log.error("listen user operator event error", e);
        }
    }
}
