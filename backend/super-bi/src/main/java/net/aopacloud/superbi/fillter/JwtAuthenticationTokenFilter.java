package net.aopacloud.superbi.fillter;

import net.aopacloud.superbi.common.core.utils.StringUtils;
import net.aopacloud.superbi.model.entity.LoginUser;
import net.aopacloud.superbi.service.TokenService;
import org.springframework.security.core.context.SecurityContextHolder;
import net.aopacloud.superbi.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/11/29 22:42
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    private final TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        LoginUser loginUser = tokenService.getLoginUser(request);
        if (StringUtils.isNotNull(loginUser) && StringUtils.isNull(SecurityUtils.getAuthentication())) {
            tokenService.verifyToken(loginUser);
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            // TODO SecurityContextHolder 需要统一，rick 2023-12-04
            net.aopacloud.superbi.common.core.context.SecurityContextHolder.setUserName(loginUser.getUsername());
            net.aopacloud.superbi.common.core.context.SecurityContextHolder.setUserId(String.valueOf(loginUser.getId()));
        }
        filterChain.doFilter(request, response);
    }
}
