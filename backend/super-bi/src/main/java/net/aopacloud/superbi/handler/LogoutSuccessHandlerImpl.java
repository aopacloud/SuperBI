package net.aopacloud.superbi.handler;

import eu.bitwalker.useragentutils.UserAgent;
import lombok.RequiredArgsConstructor;
import net.aopacloud.superbi.common.core.constant.Constants;
import net.aopacloud.superbi.common.core.utils.ServletUtils;
import net.aopacloud.superbi.common.core.utils.StringUtils;
import net.aopacloud.superbi.common.core.web.domain.RestApiResponse;
import net.aopacloud.superbi.listener.event.LoginEvent;
import net.aopacloud.superbi.model.entity.LoginUser;
import net.aopacloud.superbi.service.TokenService;
import net.aopacloud.superbi.util.JSONUtils;
import net.aopacloud.superbi.utils.IpUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/11/29 22:29
 */
@Configuration
@RequiredArgsConstructor
public class LogoutSuccessHandlerImpl implements LogoutSuccessHandler {

    private final TokenService tokenService;
    private final ApplicationContext applicationContext;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        LoginUser loginUser = tokenService.getLoginUser(request);
        UserAgent userAgent = UserAgent.parseUserAgentString(ServletUtils.getRequest().getHeader("User-Agent"));
        String ip = IpUtils.getIpAddr();
        if (StringUtils.isNotNull(loginUser)) {
            String username = loginUser.getUsername();
            // 删除用户缓存记录
            tokenService.delLoginUser(loginUser.getToken());
            // 记录用户退出日志
            LoginEvent loginEvent = LoginEvent.of(username,
                    Constants.LOGOUT,
                    "退出成功",
                    ip,
                    userAgent);
            applicationContext.publishEvent(loginEvent);
        }
        ServletUtils.renderString(response, JSONUtils.toJsonString(RestApiResponse.success("退出成功")));
    }
}
