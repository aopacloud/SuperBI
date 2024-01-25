package net.aopacloud.superbi.service.impl;

import net.aopacloud.superbi.common.core.constant.Constants;
import net.aopacloud.superbi.common.core.utils.StringUtils;
import net.aopacloud.superbi.common.redis.service.RedisService;
import net.aopacloud.superbi.constant.CacheConstants;
import net.aopacloud.superbi.constant.UserConstants;
import net.aopacloud.superbi.context.AuthenticationContextHolder;
import net.aopacloud.superbi.exception.ServiceException;
import net.aopacloud.superbi.exception.user.CaptchaException;
import net.aopacloud.superbi.exception.user.CaptchaExpireException;
import net.aopacloud.superbi.exception.user.UserNotExistsException;
import net.aopacloud.superbi.exception.user.UserPasswordNotMatchException;
import net.aopacloud.superbi.i18n.LocaleMessages;
import net.aopacloud.superbi.manager.AsyncManager;
import net.aopacloud.superbi.manager.factory.AsyncFactory;
import net.aopacloud.superbi.model.dto.CaptchaDTO;
import net.aopacloud.superbi.model.dto.TokenDTO;
import net.aopacloud.superbi.model.entity.LoginUser;
import net.aopacloud.superbi.model.entity.SysLoginLog;
import net.aopacloud.superbi.model.entity.SysUser;
import net.aopacloud.superbi.model.uo.LoginBodyUO;
import net.aopacloud.superbi.service.SysLoginService;
import net.aopacloud.superbi.service.TokenService;
import net.aopacloud.superbi.utils.IpUtils;
import net.aopacloud.superbi.utils.UuidUtils;
import com.google.code.kaptcha.Producer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.FastByteArrayOutputStream;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/11/28 20:23
 */
@Service
@RequiredArgsConstructor
public class SysLoginServiceImpl implements SysLoginService {

    private final TokenService tokenService;

    private final RedisService redisService;

    private final AuthenticationManager authenticationManager;

    private final Producer captchaProducer;

    @Override
    public TokenDTO login(LoginBodyUO loginBodyUO) {
        // 验证码校验
        validateCaptcha(loginBodyUO);
        // 登陆前置校验
        loginPreCheck(loginBodyUO);
        // 用户验证
        Authentication authentication = null;
        try {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginBodyUO.getUsername(), loginBodyUO.getPassword());
            AuthenticationContextHolder.setContext(authenticationToken);
            authentication = authenticationManager.authenticate(authenticationToken);
        } catch (Exception e) {
            if (e instanceof BadCredentialsException) {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(loginBodyUO.getUsername(), Constants.LOGIN_FAIL, LocaleMessages.getMessage("user.password.not.match")));
                throw new UserPasswordNotMatchException();
            }
            else {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(loginBodyUO.getUsername(), Constants.LOGIN_FAIL, e.getMessage()));
                throw new ServiceException(e.getMessage());
            }
        } finally {
            AuthenticationContextHolder.clearContext();
        }
        AsyncManager.me().execute(AsyncFactory.recordLogininfor(loginBodyUO.getUsername(), Constants.LOGIN_SUCCESS, LocaleMessages.getMessage("user.login.success")));
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        recordLoginInfo(loginUser.getId());
        // 生成token
        TokenDTO tokenDTO = new TokenDTO();
        tokenDTO.setToken(tokenService.createToken(loginUser));
        return tokenDTO;
    }

    public void recordLoginInfo(Long userId) {
        SysUser user = new SysUser();
        user.setId(userId);
        user.setLoginIp(IpUtils.getIpAddr());
        user.setLastOnlineTime(new Date(System.currentTimeMillis()));
        // TODO 更新用户信息
        // userService.updateUserProfile(sysUser);
    }

    /**
     * 登陆前置校验
     * @param loginBodyUO
     */
    public void loginPreCheck(LoginBodyUO loginBodyUO) {
        if (StringUtils.isEmpty(loginBodyUO.getUsername()) || StringUtils.isEmpty(loginBodyUO.getPassword())) {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(loginBodyUO.getUsername(), Constants.LOGIN_FAIL, LocaleMessages.getMessage("not.null")));
            throw new UserNotExistsException();
        }
        // 如果密码不在指定范围内 错误
        if (loginBodyUO.getPassword().length() < UserConstants.PASSWORD_MIN_LENGTH ||
                loginBodyUO.getPassword().length() > UserConstants.PASSWORD_MAX_LENGTH) {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(loginBodyUO.getUsername(), Constants.LOGIN_FAIL, LocaleMessages.getMessage("user.password.not.match")));
            throw new UserPasswordNotMatchException();
        }
        // 用户名不在指定范围内 错误
        if (loginBodyUO.getUsername().length() < UserConstants.USERNAME_MIN_LENGTH ||
                loginBodyUO.getUsername().length() > UserConstants.USERNAME_MAX_LENGTH) {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(loginBodyUO.getUsername(), Constants.LOGIN_FAIL, LocaleMessages.getMessage("user.password.not.match")));
            throw new UserPasswordNotMatchException();
        }
    }

    /**
     * 验证码校验
     * @param loginBodyUO
     */
    public void validateCaptcha(LoginBodyUO loginBodyUO) {
        String captchaRedisKey = CacheConstants.CAPTCHA_CODE_KEY + StringUtils.nvl(loginBodyUO.getUuid(), "") ;
        String captchaCode = redisService.getCacheObject(captchaRedisKey);
        redisService.deleteObject(captchaRedisKey);
        if (null == captchaCode) {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(loginBodyUO.getUsername(), Constants.LOGIN_FAIL, LocaleMessages.getMessage("user.jcaptcha.expire")));
            throw new CaptchaExpireException();
        }

        if (!captchaCode.equalsIgnoreCase(loginBodyUO.getCode())) {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(loginBodyUO.getUsername(), Constants.LOGIN_FAIL, LocaleMessages.getMessage("user.jcaptcha.error")));
            throw new CaptchaException();
        }
    }

    @Override
    public CaptchaDTO captcha() {
        String uuid = UuidUtils.simpleUuid();
        String captchaRedisKey = CacheConstants.CAPTCHA_CODE_KEY + uuid;
        CaptchaDTO captcha = new CaptchaDTO();
        String code = captchaProducer.createText();
        BufferedImage image = captchaProducer.createImage(code);
        redisService.setCacheObject(captchaRedisKey, code, CacheConstants.CAPTCHA_EXPIRATION, TimeUnit.MINUTES);

        FastByteArrayOutputStream fastByteArrayOutputStream = new FastByteArrayOutputStream();
        try {
            ImageIO.write(image, "jpg", fastByteArrayOutputStream);
        } catch (Exception e) {
            e.printStackTrace();
            // TODO 抛异常
//            return RestApiResponse.error("验证码生成失败");
        }
        captcha.setUuid(uuid);
        captcha.setImg(Base64.getEncoder().encodeToString(fastByteArrayOutputStream.toByteArray()));
        return captcha;
    }

    @Override
    public void insertSysLoginLog(SysLoginLog sysLoginLog) {

    }

}
