package net.aopacloud.superbi.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.aopacloud.superbi.common.core.constant.Constants;
import net.aopacloud.superbi.listener.event.LoginEvent;
import net.aopacloud.superbi.model.entity.SysLoginLog;
import net.aopacloud.superbi.service.SysLoginService;
import net.aopacloud.superbi.utils.LogUtils;
import org.apache.commons.lang3.StringUtils;
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
public class LoginListener {
    private final SysLoginService sysLoginService;

    @EventListener
    public void onLogin(LoginEvent event) {
        log.info("receive event {}", event);
        try {
            StringBuilder logStringBuilder = new StringBuilder();
            logStringBuilder.append(LogUtils.getBlock(event.getIp()));
            logStringBuilder.append(LogUtils.getBlock(event.getUsername()));
            logStringBuilder.append(LogUtils.getBlock(event.getStatus()));
            logStringBuilder.append(LogUtils.getBlock(event.getMessage()));
            log.info(logStringBuilder.toString(), event.getArgs());

            String os = event.getUserAgent().getOperatingSystem().getName();
            String browser = event.getUserAgent().getBrowser().getName();
            SysLoginLog sysLoginLog = new SysLoginLog();
            sysLoginLog.setUsername(event.getUsername());
            sysLoginLog.setIp(event.getIp());
            sysLoginLog.setBrowser(browser);
            sysLoginLog.setOs(os);
            sysLoginLog.setErrorMsg(event.getMessage());
            if (StringUtils.equalsAny(event.getStatus(), Constants.LOGIN_SUCCESS, Constants.LOGOUT, Constants.REGISTER)) {
                sysLoginLog.setLoginStatus(Constants.SUCCESS.toString());
            } else if (Constants.LOGIN_FAIL.equals(event.getStatus())) {
                sysLoginLog.setLoginStatus(Constants.FAIL.toString());
            }
            sysLoginService.insertSysLoginLog(sysLoginLog);
        } catch (Exception e) {
            log.error("listen user login event error", e);
        }
    }
}
