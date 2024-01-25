package net.aopacloud.superbi.service;

import net.aopacloud.superbi.model.entity.LoginUser;

import javax.servlet.http.HttpServletRequest;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/11/29 20:10
 */
public interface TokenService {
    String createToken(LoginUser loginUser);

    LoginUser getLoginUser(HttpServletRequest request);

    void delLoginUser(String token);

    void verifyToken(LoginUser loginUser);
}
