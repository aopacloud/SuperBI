package net.aopacloud.superbi.manager.factory;

import net.aopacloud.superbi.common.core.constant.Constants;
import net.aopacloud.superbi.common.core.utils.ServletUtils;
import net.aopacloud.superbi.common.core.utils.SpringUtils;
import net.aopacloud.superbi.common.core.utils.StringUtils;
import net.aopacloud.superbi.model.entity.SysOperatorLog;
import net.aopacloud.superbi.model.entity.SysLoginLog;
import net.aopacloud.superbi.service.SysLoginService;
import net.aopacloud.superbi.service.SysOperatorLogService;
import net.aopacloud.superbi.utils.IpUtils;
import net.aopacloud.superbi.utils.LogUtils;
import eu.bitwalker.useragentutils.UserAgent;
import lombok.extern.slf4j.Slf4j;

import java.util.TimerTask;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/12/1 11:01
 */
@Slf4j
public class AsyncFactory {
    /**
     * 记录登录信息
     *
     * @param username 用户名
     * @param status 状态
     * @param message 消息
     * @param args 列表
     * @return 任务task
     */
    public static TimerTask recordLogininfor(final String username, final String status, final String message,
                                             final Object... args)
    {
        final UserAgent userAgent = UserAgent.parseUserAgentString(ServletUtils.getRequest().getHeader("User-Agent"));
        final String ip = IpUtils.getIpAddr();
        return new TimerTask()
        {
            @Override
            public void run()
            {
                StringBuilder s = new StringBuilder();
                s.append(LogUtils.getBlock(ip));
                s.append(LogUtils.getBlock(username));
                s.append(LogUtils.getBlock(status));
                s.append(LogUtils.getBlock(message));
                // 打印信息到日志
                log.info(s.toString(), args);
                // 获取客户端操作系统
                String os = userAgent.getOperatingSystem().getName();
                // 获取客户端浏览器
                String browser = userAgent.getBrowser().getName();
                // 封装对象
                SysLoginLog logininfor = new SysLoginLog();
                logininfor.setUsername(username);
                logininfor.setIp(ip);
                logininfor.setBrowser(browser);
                logininfor.setOs(os);
                logininfor.setErrorMsg(message);
                // 日志状态
                if (StringUtils.equalsAny(status, Constants.LOGIN_SUCCESS, Constants.LOGOUT, Constants.REGISTER))
                {
                    logininfor.setLoginStatus(Constants.SUCCESS);
                }
                else if (Constants.LOGIN_FAIL.equals(status))
                {
                    logininfor.setLoginStatus(Constants.FAIL);
                }
                // 插入数据
                SpringUtils.getBean(SysLoginService.class).insertSysLoginLog(logininfor);
            }
        };
    }

    /**
     * 操作日志记录
     *
     * @param operLog 操作日志信息
     * @return 任务task
     */
    public static TimerTask recordOper(final SysOperatorLog operLog)
    {
        return new TimerTask()
        {
            @Override
            public void run()
            {
                // 远程查询操作地点
                SpringUtils.getBean(SysOperatorLogService.class).insertOperatorLog(operLog);
            }
        };
    }
}
