package net.aopacloud.superbi.handler;

import net.aopacloud.superbi.common.core.constant.Constants;
import net.aopacloud.superbi.common.core.utils.ServletUtils;
import net.aopacloud.superbi.common.core.utils.StringUtils;
import net.aopacloud.superbi.common.core.web.domain.RestApiResponse;
import net.aopacloud.superbi.util.JSONUtils;
import net.aopacloud.superbi.manager.AsyncManager;
import net.aopacloud.superbi.manager.factory.AsyncFactory;
import net.aopacloud.superbi.model.entity.LoginUser;
import net.aopacloud.superbi.service.TokenService;
import lombok.RequiredArgsConstructor;
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

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        LoginUser loginUser = tokenService.getLoginUser(request);
        if (StringUtils.isNotNull(loginUser)) {
            String username = loginUser.getUsername();
            // 删除用户缓存记录
            tokenService.delLoginUser(loginUser.getToken());
            // 记录用户退出日志
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(loginUser.getUsername(), Constants.LOGOUT, "退出成功"));
        }
        ServletUtils.renderString(response, JSONUtils.toJsonString(RestApiResponse.success("退出成功")));
    }
}
